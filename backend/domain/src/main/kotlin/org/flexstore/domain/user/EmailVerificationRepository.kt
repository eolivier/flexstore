package org.flexstore.domain.user

interface EmailVerificationRepository {
    fun save(verification: EmailVerification)
    fun findBy(token: VerificationToken): EmailVerification?
    fun isUnknown(token: VerificationToken): Boolean = findBy(token) == null
    // A new link replaces the previous ones, and a verified email needs no link any more
    fun deleteAllOf(userId: UserId)
}
