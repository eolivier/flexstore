package org.flexstore.domain.email

import org.flexstore.domain.email.model.AbandonedCartReminderEmail
import org.flexstore.domain.email.model.EmailVerification
import org.flexstore.domain.email.model.EmailVerificationLink
import org.flexstore.domain.email.model.ExpirationDate
import org.flexstore.domain.email.model.OrderConfirmationEmail
import org.flexstore.domain.email.model.OrderStatusEmail
import org.flexstore.domain.email.model.PasswordResetEmail
import org.flexstore.domain.email.model.VerificationToken
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.email.model.VerificationLinkUnknownException

// The emails of the shop: sends them (the infrastructure decides how: SMTP, logs...) and keeps the verifications
// of the email addresses, whose links are sent by email
interface EmailRepository {
    fun send(email: OrderConfirmationEmail)
    fun send(email: OrderStatusEmail)
    fun send(link: EmailVerificationLink)
    fun send(email: PasswordResetEmail)
    fun send(email: AbandonedCartReminderEmail)

    fun save(verification: EmailVerification)
    fun findBy(token: VerificationToken): EmailVerification?
    // The verification, which a precondition has checked to exist
    fun get(token: VerificationToken): EmailVerification =
        findBy(token) ?: throw VerificationLinkUnknownException("Unknown verification link.")
    fun isUnknown(token: VerificationToken): Boolean = findBy(token) == null
    // The links of the user expire at the given date, unless they already expired before
    fun expireAllVerificationsOf(userId: UserId, expiration: ExpirationDate)

    // A verified email needs no link any more
    fun deleteAllVerificationsOf(userId: UserId)
}
