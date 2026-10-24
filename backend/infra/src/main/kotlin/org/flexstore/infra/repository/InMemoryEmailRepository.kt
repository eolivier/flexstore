package org.flexstore.infra.repository

import org.flexstore.domain.email.*
import org.flexstore.domain.user.model.UserId

// Keeps the emails instead of sending them, and the verifications in memory: for the tests
class InMemoryEmailRepository : EmailRepository {

    private val sentEmails = mutableListOf<OrderConfirmationEmail>()
    private val sentVerificationLinks = mutableListOf<EmailVerificationLink>()
    private val verifications = mutableListOf<EmailVerification>()

    override fun send(email: OrderConfirmationEmail) {
        sentEmails.add(email)
    }

    override fun send(link: EmailVerificationLink) {
        sentVerificationLinks.add(link)
    }

    override fun save(verification: EmailVerification) {
        verifications.add(verification)
    }

    override fun findBy(token: VerificationToken): EmailVerification? = verifications.firstOrNull { it.token == token }

    override fun deleteAllVerificationsOf(userId: UserId) {
        verifications.removeIf { it.userId == userId }
    }

    fun sentEmails(): List<OrderConfirmationEmail> = sentEmails.toList()

    fun sentVerificationLinks(): List<EmailVerificationLink> = sentVerificationLinks.toList()

    fun clear() {
        sentEmails.clear()
        sentVerificationLinks.clear()
        verifications.clear()
    }
}
