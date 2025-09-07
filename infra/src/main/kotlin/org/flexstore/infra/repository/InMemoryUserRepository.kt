package org.flexstore.infra.repository

import org.flexstore.domain.entity.Email
import org.flexstore.domain.entity.User
import org.flexstore.domain.entity.UserId
import org.flexstore.domain.repository.UserRepository
import org.ucop.domain.entity.Name

class InMemoryUserRepository : UserRepository {

    override fun exists(userId: UserId): Boolean = users.containsKey(userId)

    override fun notExists(userId: UserId): Boolean = !exists(userId)

    override fun notExistsBasedOn(email: Email): Boolean {
        TODO("Not yet implemented")
    }

    override fun save(user: User): User {
        users[user.id] = user
        return user
    }

    override fun findById(id: UserId): User = users[id] ?: throw IllegalArgumentException("User not found")
    override fun findAll(): List<User> = users.values.toList()
    override fun delete(id: UserId): Boolean = users.remove(id) != null

    private val users = mutableMapOf<UserId, User>(
        UserId.ValidUserId("u-1") to User.DefinedUser(
            id = UserId.ValidUserId("u-1"),
            name = Name("Alice Smith"),
            email = Email("alice.smith@example.com")
        ),
        UserId.ValidUserId("u-2") to User.DefinedUser(
            id = UserId.ValidUserId("u-2"),
            name = Name("Bob Johnson"),
            email = Email("bob.johnson@example.com")
        ),
        UserId.ValidUserId("u-3") to User.DefinedUser(
            id = UserId.ValidUserId("u-3"),
            name = Name("Charlie Brown"),
            email = Email("charlie.brown@example.com")
        )
    )
}