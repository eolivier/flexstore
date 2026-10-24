package org.flexstore.infra.repository

import org.flexstore.domain.order.*

// Keeps the emails instead of sending them: for the tests
class InMemoryEmailRepository : EmailRepository {

    private val sentEmails = mutableListOf<OrderConfirmationEmail>()

    override fun send(email: OrderConfirmationEmail) {
        sentEmails.add(email)
    }

    fun sentEmails(): List<OrderConfirmationEmail> = sentEmails.toList()

    fun clear() = sentEmails.clear()
}
