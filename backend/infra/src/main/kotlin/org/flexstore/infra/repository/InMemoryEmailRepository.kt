package org.flexstore.infra.repository

import org.flexstore.domain.email.*
import org.flexstore.domain.user.model.UserId

// Keeps the emails instead of sending them, and the verifications in memory: for the tests
class InMemoryEmailRepository : EmailRepository {

    private val sentEmails = mutableListOf<OrderConfirmationEmail>()
    private val sentVerificationLinks = mutableListOf<EmailVerificationLink>()
    private val verifications = mutableListOf<EmailVerification>()
    private val sentPasswordResetEmails = mutableListOf<PasswordResetEmail>()
    private val sentOrderStatusEmails = mutableListOf<OrderStatusEmail>()

    override fun send(email: OrderConfirmationEmail) {
        sentEmails.add(email)
    }

    override fun send(email: OrderStatusEmail) {
        sentOrderStatusEmails.add(email)
    }

    override fun send(link: EmailVerificationLink) {
        sentVerificationLinks.add(link)
    }

    override fun send(email: PasswordResetEmail) {
        sentPasswordResetEmails.add(email)
    }

    override fun save(verification: EmailVerification) {
        verifications.add(verification)
    }

    override fun findBy(token: VerificationToken): EmailVerification? =
        verifications.firstOrNull { it.token == token }

    override fun expireAllVerificationsOf(userId: UserId, expiration: ExpirationDate) {
        verifications.replaceAll { verification ->
            val ofTheUser = verification.userId == userId
            val expiresLater = expiration.value.isBefore(verification.expiresAt.value)
            if (ofTheUser && expiresLater) verification.copy(expiresAt = expiration) else verification
        }
    }

    override fun deleteAllVerificationsOf(userId: UserId) {
        verifications.removeIf { it.userId == userId }
    }

    fun sentEmails(): List<OrderConfirmationEmail> = sentEmails.toList()

    fun sentVerificationLinks(): List<EmailVerificationLink> = sentVerificationLinks.toList()

    fun sentPasswordResetEmails(): List<PasswordResetEmail> = sentPasswordResetEmails.toList()

    fun sentOrderStatusEmails(): List<OrderStatusEmail> = sentOrderStatusEmails.toList()

    fun clear() {
        sentEmails.clear()
        sentVerificationLinks.clear()
        verifications.clear()
        sentPasswordResetEmails.clear()
        sentOrderStatusEmails.clear()
    }
}
