package org.flexstore.domain.user.repository

import org.flexstore.domain.sharedkernel.Page
import org.flexstore.domain.sharedkernel.PageRequest
import org.flexstore.domain.user.model.*
import org.ucop.domain.ErrorMessage

interface UserRepository {
    fun exists(userId: UserId): Boolean
    fun notExists(userId: UserId): Boolean
    fun notExistsBasedOn(email: Email): Boolean
    fun save(user: User): User
    fun findById(id: UserId):User
    fun findByEmail(email: Email): User
    fun findAll(): List<User>

    // A page of the users, the first created first, with the number of all the users
    fun findAll(pageRequest: PageRequest): Page<Users>
    fun delete(id: UserId):Boolean
    fun passwordMatches(email: Email, password: Password): Boolean
    fun passwordDoesNotMatch(email: Email, password: Password): Boolean = !passwordMatches(email, password)
    fun emailIsAlreadyUsed(email: Email): Boolean = !notExistsBasedOn(email)

    fun emailIsUsedByAnotherUser(email: Email, userId: UserId): Boolean =
        findByEmail(email).let { user -> user is User.DefinedUser && user.id != userId }

    // The status of the account of an existing user
    fun statusOf(userId: UserId): AccountStatus = (findById(userId) as User.DefinedUser).status

    // A customer must be an existing user whose account is not suspended, locked or disabled to act
    fun verifyCustomerCanAct(customerId: UserId) {
        if (notExists(customerId)) {
            throw UserNotFoundException(ErrorMessage("User ${customerId.value} not found."))
        }
        val status = statusOf(customerId)
        if (status.refusesSignIn()) {
            val message = ErrorMessage("The account ${customerId.value} is $status: it cannot act.")
            throw AccountRefusedException(status, message)
        }
    }
}
