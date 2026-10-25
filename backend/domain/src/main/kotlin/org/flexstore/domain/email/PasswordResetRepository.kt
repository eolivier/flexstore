package org.flexstore.domain.email

import org.flexstore.domain.user.model.UserId

// The password resets whose link still works: the last one asked by each customer
interface PasswordResetRepository {
    fun save(reset: PasswordReset)
    fun findBy(token: VerificationToken): PasswordReset?
    fun isUnknown(token: VerificationToken): Boolean = findBy(token) == null

    // A new link replaces the previous ones, and a link used to choose a password cannot be used again
    fun deleteAllOf(userId: UserId)
}
