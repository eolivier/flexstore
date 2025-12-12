package org.flexstore.infra.spring.adapter.jpa

import org.flexstore.domain.entity.*
import org.flexstore.domain.repository.UserRepository
import org.flexstore.domain.valueobject.Name
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Repository

@Repository
interface JpaUserRepository : JpaRepository<UserEntity, String> {
    fun existsByEmail(email: String): Boolean
    fun findByEmail(email: String): UserEntity?
}

@Repository
@Primary
class PostgresUserRepositoryAdapter(
    private val jpaRepo: JpaUserRepository
) : UserRepository {
    
    private val passwordEncoder = BCryptPasswordEncoder()

    override fun exists(userId: UserId): Boolean = jpaRepo.existsById(userId.value)

    override fun notExists(userId: UserId): Boolean = !exists(userId)

    override fun notExistsBasedOn(email: Email): Boolean = !jpaRepo.existsByEmail(email.value)

    override fun save(user: User): User {
        var savedUser = user
        when(user) {
            is User.DefinedUser -> {
                val hashedPasswordValue = hashPasswordIfNeeded(user.password)
                
                val entity = UserEntity(
                    id = user.id.value,
                    name = user.name.value,
                    email = user.email.value,
                    password = hashedPasswordValue
                )
                val saved = jpaRepo.save(entity)
                savedUser = saved.toDomain()
            }
            else -> IllegalArgumentException("Cannot save undefined user with id ${user.id.value}")
        }
        return savedUser
    }
    
    private fun hashPasswordIfNeeded(password: Password): String {
        return when (password) {
            is PlainPassword -> passwordEncoder.encode(password.value)
            is HashedPassword -> password.value
        }
    }

    override fun findById(id: UserId): User =
        jpaRepo.findById(id.value)
            .orElseThrow { NoSuchElementException("User ${id.value} not found") }
            .toDomain()

    override fun findByEmail(email: Email): User {
        val entity = jpaRepo.findByEmail(email.value)
        return entity?.toDomain() ?: User.UndefinedUser(UserId.InvalidUserId(""), "User not found with email ${email.value}")
    }

    override fun findAll(): List<User> = jpaRepo.findAll().map { it.toDomain() }

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
        name = Name(this.name),
        email = Email(this.email),
        password = HashedPassword(this.password)
    )
}
