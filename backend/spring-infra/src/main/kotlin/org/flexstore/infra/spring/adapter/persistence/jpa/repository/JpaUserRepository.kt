package org.flexstore.infra.spring.adapter.persistence.jpa.repository

import org.flexstore.domain.user.repository.*
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserAddressEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.entity.UserEntity
import org.flexstore.infra.spring.adapter.persistence.jpa.toDomain
import org.flexstore.infra.spring.adapter.persistence.jpa.toEntity
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.twofactor.model.TwoFactorAuthentication
import org.flexstore.domain.twofactor.model.TwoFactorSecret
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.infra.spring.config.USERS_CACHE
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.domain.Sort
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Repository

@Repository
interface JpaUserRepository : JpaRepository<UserEntity, String> {
    fun existsByEmail(email: String): Boolean
    fun findByEmail(email: String): UserEntity?
}

// The read of a user by id is cached under the id of the user; every insert, update and delete evicts that id
// only, the other users stay cached. Not cached: the reads by email (an update of the email would leave the
// entry of the previous email behind), the existence of a user and the list of the users (it holds them all)
@Repository
class PostgresUserRepositoryAdapter(
    private val jpaRepo: JpaUserRepository
) : UserRepository {

    private val passwordEncoder = BCryptPasswordEncoder()

    override fun exists(userId: UserId): Boolean = jpaRepo.existsById(userId.value)

    override fun notExists(userId: UserId): Boolean = !exists(userId)

    override fun notExistsBasedOn(email: Email): Boolean = !jpaRepo.existsByEmail(email.value)

    @CacheEvict(USERS_CACHE, key = "#result.id.value")
    override fun save(user: User): User {
        var savedUser = user
        when(user) {
            is User.DraftUser -> {
                val hashedPasswordValue = hashPasswordIfNeeded(user.password)
                val entity = UserEntity(
                    id = generateNewUserId(),
                    firstName = user.fullName.firstName.value,
                    lastName = user.fullName.lastName.value,
                    email = user.email.value,
                    password = hashedPasswordValue,
                    role = user.role.name
                )
                val saved = jpaRepo.save(entity)
                savedUser = saved.toDomain()
            }
            is User.DefinedUser -> {
                val hashedPasswordValue = hashPasswordIfNeeded(user.password)
                val company = (user.customerType as? CustomerType.Business)?.company
                val entity = UserEntity(
                    id = user.id.value,
                    firstName = user.fullName.firstName.value,
                    lastName = user.fullName.lastName.value,
                    email = user.email.value,
                    password = hashedPasswordValue,
                    role = user.role.name,
                    status = user.status.name,
                    phoneNumber = user.phoneNumber?.value,
                    twoFactorSecret = twoFactorSecretColumnOf(user.twoFactorAuthentication),
                    twoFactorStatus = twoFactorStatusColumnOf(user.twoFactorAuthentication),
                    addresses = addressEntities(user),
                    companyName = company?.name?.value,
                    companySiren = company?.siren?.value,
                    companyVatNumber = company?.vatNumber?.value?.ifEmpty { null }
                )
                val saved = jpaRepo.save(entity)
                savedUser = saved.toDomain()
            }
            else -> throw IllegalArgumentException("Cannot save undefined user with id ${user.id.value}")
        }
        return savedUser
    }

    private fun generateNewUserId(): String {
        return Identity.nextId().value
    }

    private fun twoFactorSecretColumnOf(twoFactorAuthentication: TwoFactorAuthentication): String? =
        when (twoFactorAuthentication) {
            is TwoFactorAuthentication.Disabled -> null
            is TwoFactorAuthentication.PendingConfirmation -> twoFactorAuthentication.secret.value
            is TwoFactorAuthentication.Enabled -> twoFactorAuthentication.secret.value
        }

    private fun twoFactorStatusColumnOf(twoFactorAuthentication: TwoFactorAuthentication): String? =
        when (twoFactorAuthentication) {
            is TwoFactorAuthentication.Disabled -> null
            is TwoFactorAuthentication.PendingConfirmation -> "PENDING"
            is TwoFactorAuthentication.Enabled -> "ENABLED"
        }

    private fun hashPasswordIfNeeded(password: Password): String {
        return when (password) {
            is PlainPassword -> passwordEncoder.encode(password.value)
            is HashedPassword -> password.value
        }
    }

    // The stored rows of an address that did not change are kept (same ids, same creation dates);
    // the others are new rows
    private fun addressEntities(user: User.DefinedUser): List<UserAddressEntity> {
        val storedUser = jpaRepo.findById(user.id.value)
        val storedUserAddresses = storedUser.map { it.addresses }.orElse(emptyList()).toMutableList()
        return user.postalAddresses.asList().mapIndexed { position, address ->
            val stored = storedUserAddresses.firstOrNull { it.address.toDomain() == address }
            if (stored != null) {
                storedUserAddresses.remove(stored)
                stored.copy(position = position).also { it.createdAt = stored.createdAt }
            } else {
                val addressEntity = address.toEntity(Identity.nextId().value)
                UserAddressEntity(Identity.nextId().value, addressEntity, position)
            }
        }
    }

    @Cacheable(USERS_CACHE, key = "#p0.value")
    override fun findById(id: UserId): User =
        jpaRepo.findById(id.value)
            .orElseThrow { NoSuchElementException("User ${id.value} not found") }
            .toDomain()

    override fun findByEmail(email: Email): User {
        val entity = jpaRepo.findByEmail(email.value)
        return entity?.toDomain() ?: User.UndefinedUser(
            UserId.InvalidUserId(""), "User not found with email ${email.value}"
        )
    }

    override fun findAll(): List<User> = jpaRepo.findAll().map { it.toDomain() }

    // The first created first; the id keeps the order of two users created at the same moment stable
    override fun findAll(pageRequest: PageRequest): Page<Users> {
        val pageable = org.springframework.data.domain.PageRequest.of(
            pageRequest.number.value, pageRequest.size.value, Sort.by("createdAt", "id")
        )
        val userEntities = jpaRepo.findAll(pageable)
        return Page(Users(userEntities.content.map { it.toDomain() }), TotalCount(userEntities.totalElements))
    }

    @CacheEvict(USERS_CACHE, key = "#p0.value")
    override fun delete(id: UserId): Boolean {
        return if (jpaRepo.existsById(id.value)) {
            jpaRepo.deleteById(id.value)
            true
        } else {
           false
        }
    }

    override fun passwordMatches(email: Email, password: Password): Boolean {
        val user = findByEmail(email)
        return when (user) {
            is User.DefinedUser -> when (user.password) {
                is HashedPassword -> passwordEncoder.matches(password.value, user.password.value)
                else -> false
            }
            else -> false
        }
    }

    // mapping helper
    private fun UserEntity.toDomain() = User.DefinedUser(
        id = UserId.ValidUserId(this.id),
        fullName = FullName(FirstName(this.firstName), LastName(this.lastName)),
        email = Email(this.email),
        password = HashedPassword(this.password),
        role = Role.valueOf(this.role),
        status = AccountStatus.valueOf(this.status),
        phoneNumber = phoneNumber?.let { PhoneNumber(it) },
        postalAddresses = PostalAddresses(addresses.map { it.address.toDomain() }),
        customerType = customerType(),
        twoFactorAuthentication = twoFactorAuthenticationOf(twoFactorSecret, twoFactorStatus)
    )

    private fun twoFactorAuthenticationOf(
        secretColumn: String?,
        statusColumn: String?
    ): TwoFactorAuthentication {
        if (secretColumn == null) return TwoFactorAuthentication.Disabled
        val secret = TwoFactorSecret(secretColumn)
        return if (statusColumn == "ENABLED") {
            TwoFactorAuthentication.Enabled(secret)
        } else {
            TwoFactorAuthentication.PendingConfirmation(secret)
        }
    }

    // The company is given by its name and its SIREN together: without them the customer buys for themselves
    private fun UserEntity.customerType(): CustomerType {
        val name = companyName ?: return CustomerType.Individual
        val siren = companySiren ?: return CustomerType.Individual
        val company = Company(CompanyName(name), VatNumber(companyVatNumber.orEmpty()))
        return CustomerType.Business(CustomerCompany(company, Siren(siren)))
    }
}
