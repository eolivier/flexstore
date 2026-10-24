package org.flexstore.domain.user

interface UserRepository {
    fun exists(userId: UserId): Boolean
    fun notExists(userId: UserId): Boolean
    fun notExistsBasedOn(email: Email): Boolean
    fun save(user: User): User
    fun findById(id: UserId):User
    fun findByEmail(email: Email): User
    fun findAll(): List<User>
    fun delete(id: UserId):Boolean
    fun passwordMatches(email: Email, password: Password): Boolean
    fun passwordDoesNotMatch(email: Email, password: Password): Boolean = !passwordMatches(email, password)
    fun emailIsAlreadyUsed(email: Email): Boolean = !notExistsBasedOn(email)

    fun emailIsUsedByAnotherUser(email: Email, userId: UserId): Boolean =
        findByEmail(email).let { user -> user is User.DefinedUser && user.id != userId }

    // Managing the users (create, edit) is reserved to an existing user with the ADMIN role
    fun isNotAdministrator(userId: UserId): Boolean {
        if (notExists(userId)) return true
        val user = findById(userId)
        return user !is User.DefinedUser || user.isNotAdministrator()
    }
}
