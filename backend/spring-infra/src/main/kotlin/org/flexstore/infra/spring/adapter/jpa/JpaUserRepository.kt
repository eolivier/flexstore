package org.flexstore.infra.spring.adapter.jpa

import org.flexstore.domain.entity.Email
import org.flexstore.domain.entity.User
import org.flexstore.domain.entity.UserId
import org.flexstore.domain.repository.UserRepository
import org.flexstore.domain.valueobject.Name
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface JpaUserRepository : JpaRepository<UserEntity, String> {
    fun existsByEmail(email: String): Boolean
}

@Repository
@Primary
class PostgresUserRepositoryAdapter(
    private val jpaRepo: JpaUserRepository
) : UserRepository {

    override fun exists(userId: UserId): Boolean = jpaRepo.existsById(userId.value)

    override fun notExists(userId: UserId): Boolean = !exists(userId)

    override fun notExistsBasedOn(email: Email): Boolean = !jpaRepo.existsByEmail(email.value)

    override fun save(user: User): User {
        var savedUser = user
        when(user) {
            is User.DefinedUser -> {
                val entity = UserEntity(
                    id = user.id.value,
                    name = user.name.value,
                    email = user.email.value
                )
                val saved = jpaRepo.save(entity)
                savedUser = saved.toDomain()
            }
            else -> IllegalArgumentException("Cannot save undefined user with id ${user.id.value}")
        }
        return savedUser
    }

    override fun findById(id: UserId): User =
        jpaRepo.findById(id.value)
            .orElseThrow { NoSuchElementException("User ${id.value} not found") }
            .toDomain()

    override fun findAll(): List<User> = jpaRepo.findAll().map { it.toDomain() }

    override fun delete(id: UserId): Boolean {
        return if (jpaRepo.existsById(id.value)) {
            jpaRepo.deleteById(id.value)
            true
        } else {
           false
        }
    }

    // mapping helper
    private fun UserEntity.toDomain() = User.DefinedUser(
        id = UserId.ValidUserId(this.id),
        name = Name(this.name),
        email = Email(this.email)
    )
}
