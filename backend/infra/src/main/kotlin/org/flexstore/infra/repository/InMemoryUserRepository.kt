package org.flexstore.infra.repository

import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import at.favre.lib.crypto.bcrypt.BCrypt

class InMemoryUserRepository : UserRepository {
    
    private val bcryptHasher = BCrypt.withDefaults()
    private val bcryptVerifier = BCrypt.verifyer()

    override fun exists(userId: UserId): Boolean = users.containsKey(userId)

    override fun notExists(userId: UserId): Boolean = !exists(userId)

    override fun notExistsBasedOn(email: Email): Boolean = findByEmail(email) !is User.DefinedUser

    override fun save(user: User): User {
        val userToSave = when (user) {
            is User.DefinedUser -> hashPasswordIfNeeded(user)
            is User.DraftUser -> hashPasswordIfNeeded(
                User.DefinedUser(UserId.ValidUserId(Identity.nextId().value), user.fullName, user.email, user.password, user.role)
            )
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
                    val verification = bcryptVerifier.verify(password.value.toCharArray(), user.password.value.toCharArray())
                    verification.verified
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
                fullName = FullName(FirstName("Alice"), LastName("Smith")),
                email = Email("alice.smith@example.com"),
                password = HashedPassword(bcryptHasher.hashToString(12, "password123".toCharArray()))
            ),
            User.DefinedUser(
                id = UserId.ValidUserId("u-2"),
                fullName = FullName(FirstName("Bob"), LastName("Johnson")),
                email = Email("bob.johnson@example.com"),
                password = HashedPassword(bcryptHasher.hashToString(12, "password456".toCharArray()))
            ),
            User.DefinedUser(
                id = UserId.ValidUserId("u-3"),
                fullName = FullName(FirstName("Charlie"), LastName("Brown")),
                email = Email("charlie.brown@example.com"),
                password = HashedPassword(bcryptHasher.hashToString(12, "password789".toCharArray()))
            )
        )
        demoUsers.forEach { users[it.id] = it }
    }
}