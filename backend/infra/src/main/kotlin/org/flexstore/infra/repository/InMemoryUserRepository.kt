package org.flexstore.infra.repository

import at.favre.lib.crypto.bcrypt.BCrypt
import org.flexstore.domain.entity.*
import org.flexstore.domain.repository.UserRepository
import org.flexstore.domain.valueobject.Name

class InMemoryUserRepository : UserRepository {
    
    private val bcryptHasher = BCrypt.withDefaults()
    private val bcryptVerifier = BCrypt.verifyer()

    override fun exists(userId: UserId): Boolean = users.containsKey(userId)

    override fun notExists(userId: UserId): Boolean = !exists(userId)

    override fun notExistsBasedOn(email: Email): Boolean {
        TODO("Not yet implemented")
    }

    override fun save(user: User): User {
        val userToSave = when (user) {
            is User.DefinedUser -> hashPasswordIfNeeded(user)
            else -> user
        }
        users[userToSave.id] = userToSave
        return userToSave
    }
    
    private fun hashPasswordIfNeeded(user: User.DefinedUser): User.DefinedUser {
        return when (user.password) {
            is PlainPassword -> {
                val hashedPassword = bcryptHasher.hashToString(12, user.password.value.toCharArray())
                user.copy(password = HashedPassword(hashedPassword))
            }
            is HashedPassword -> user
        }
    }

    override fun findById(id: UserId): User = users[id] ?: throw IllegalArgumentException("User not found")
    
    override fun findByEmail(email: Email): User {
        return users.values.find { 
            it is User.DefinedUser && it.email.value == email.value 
        } ?: User.UndefinedUser(UserId.InvalidUserId(""), "User not found with email ${email.value}")
    }
    
    override fun findAll(): List<User> = users.values.toList()
    
    override fun delete(id: UserId): Boolean = users.remove(id) != null
    
    override fun passwordMatches(email: Email, password: Password): Boolean {
        val user = findByEmail(email)
        return when (user) {
            is User.DefinedUser -> when (user.password) {
                is HashedPassword -> {
                    val result = bcryptVerifier.verify(password.value.toCharArray(), user.password.value.toCharArray())
                    result.verified
                }
                else -> false
            }
            else -> false
        }
    }

    private val users = mutableMapOf<UserId, User>()
    
    init {
        // Initialize with pre-hashed passwords for demo users
        val demoUsers = listOf(
            User.DefinedUser(
                id = UserId.ValidUserId("u-1"),
                name = Name("Alice Smith"),
                email = Email("alice.smith@example.com"),
                password = HashedPassword(bcryptHasher.hashToString(12, "password123".toCharArray()))
            ),
            User.DefinedUser(
                id = UserId.ValidUserId("u-2"),
                name = Name("Bob Johnson"),
                email = Email("bob.johnson@example.com"),
                password = HashedPassword(bcryptHasher.hashToString(12, "password456".toCharArray()))
            ),
            User.DefinedUser(
                id = UserId.ValidUserId("u-3"),
                name = Name("Charlie Brown"),
                email = Email("charlie.brown@example.com"),
                password = HashedPassword(bcryptHasher.hashToString(12, "password789".toCharArray()))
            )
        )
        demoUsers.forEach { users[it.id] = it }
    }
}