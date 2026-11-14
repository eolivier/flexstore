package org.flexstore.domain.email

import org.flexstore.domain.email.model.PasswordReset
import org.flexstore.domain.email.model.VerificationToken
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.email.model.PasswordResetLinkUnknownException

// The password resets whose link still works: the last one asked by each customer
interface PasswordResetRepository {
    fun save(reset: PasswordReset)
    fun findBy(token: VerificationToken): PasswordReset?
    // The password reset, which a precondition has checked to exist
    fun get(token: VerificationToken): PasswordReset =
        findBy(token) ?: throw PasswordResetLinkUnknownException("Unknown password reset link.")
    fun isUnknown(token: VerificationToken): Boolean = findBy(token) == null

    fun isKnown(token: VerificationToken): Boolean = !isUnknown(token)

    // A new link replaces the previous ones, and a link used to choose a password cannot be used again
    fun deleteAllOf(userId: UserId)
}
