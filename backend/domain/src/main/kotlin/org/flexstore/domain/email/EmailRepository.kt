package org.flexstore.domain.email

import org.flexstore.domain.user.UserId

// The emails of the shop: sends them (the infrastructure decides how: SMTP, logs...) and keeps the verifications
// of the email addresses, whose links are sent by email
interface EmailRepository {
    fun send(email: OrderConfirmationEmail)
    fun send(link: EmailVerificationLink)

    fun save(verification: EmailVerification)
    fun findBy(token: VerificationToken): EmailVerification?
    fun isUnknown(token: VerificationToken): Boolean = findBy(token) == null
    // A new link replaces the previous ones, and a verified email needs no link any more
    fun deleteAllVerificationsOf(userId: UserId)
}
