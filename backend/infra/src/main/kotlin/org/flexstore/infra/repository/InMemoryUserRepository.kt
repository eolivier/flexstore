package org.flexstore.infra.repository

import org.flexstore.domain.entity.*
import org.flexstore.domain.repository.UserRepository
import org.flexstore.domain.valueobject.Name
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class InMemoryUserRepository : UserRepository {
    
    private val passwordEncoder = BCryptPasswordEncoder()

    override fun exists(userId: UserId): Boolean = users.containsKey(userId)

    override fun notExists(userId: UserId): Boolean = !exists(userId)

    override fun notExistsBasedOn(email: Email): Boolean {
        TODO("Not yet implemented")
    }

    override fun save(user: User): User {
        // Hash password if it's a DefinedUser with a plain password
        val userToSave = when (user) {
            is User.DefinedUser -> {
                // Only hash if password doesn't look already hashed (BCrypt hashes start with $2a$, $2b$, or $2y$)
                val hashedPassword = if (user.password.value.matches(Regex("^\\$2[aby]\\$.*"))) {
                    user.password
                } else {
                    Password(passwordEncoder.encode(user.password.value))
                }
                user.copy(password = hashedPassword)
            }
            else -> user
        }
        users[userToSave.id] = userToSave
        return userToSave
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
            is User.DefinedUser -> passwordEncoder.matches(password.value, user.password.value)
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
                password = Password(passwordEncoder.encode("password123"))
            ),
            User.DefinedUser(
                id = UserId.ValidUserId("u-2"),
                name = Name("Bob Johnson"),
                email = Email("bob.johnson@example.com"),
                password = Password(passwordEncoder.encode("password456"))
            ),
            User.DefinedUser(
                id = UserId.ValidUserId("u-3"),
                name = Name("Charlie Brown"),
                email = Email("charlie.brown@example.com"),
                password = Password(passwordEncoder.encode("password789"))
            )
        )
        demoUsers.forEach { users[it.id] = it }
    }
}