package org.flexstore.domain.email

import org.flexstore.domain.user.model.UserId

// The emails of the shop: sends them (the infrastructure decides how: SMTP, logs...) and keeps the verifications
// of the email addresses, whose links are sent by email
interface EmailRepository {
    fun send(email: OrderConfirmationEmail)
    fun send(email: OrderStatusEmail)
    fun send(link: EmailVerificationLink)
    fun send(email: PasswordResetEmail)

    fun save(verification: EmailVerification)
    fun findBy(token: VerificationToken): EmailVerification?
    fun isUnknown(token: VerificationToken): Boolean = findBy(token) == null
    // The links of the user expire at the given date, unless they already expired before
    fun expireAllVerificationsOf(userId: UserId, expiration: ExpirationDate)

    // A verified email needs no link any more
    fun deleteAllVerificationsOf(userId: UserId)
}
