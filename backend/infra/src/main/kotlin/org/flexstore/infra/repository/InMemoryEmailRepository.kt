package org.flexstore.infra.repository

import org.flexstore.domain.order.*
import org.flexstore.domain.user.AccountEmailRepository
import org.flexstore.domain.user.EmailVerificationEmail

// Keeps the emails instead of sending them: for the tests
class InMemoryEmailRepository : EmailRepository, AccountEmailRepository {

    private val sentEmails = mutableListOf<OrderConfirmationEmail>()
    private val sentVerificationEmails = mutableListOf<EmailVerificationEmail>()

    override fun send(email: OrderConfirmationEmail) {
        sentEmails.add(email)
    }

    override fun send(email: EmailVerificationEmail) {
        sentVerificationEmails.add(email)
    }

    fun sentEmails(): List<OrderConfirmationEmail> = sentEmails.toList()

    fun sentVerificationEmails(): List<EmailVerificationEmail> = sentVerificationEmails.toList()

    fun clear() {
        sentEmails.clear()
        sentVerificationEmails.clear()
    }
}
