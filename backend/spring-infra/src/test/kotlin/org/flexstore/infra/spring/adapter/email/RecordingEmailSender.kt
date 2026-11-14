package org.flexstore.infra.spring.adapter.email

import org.flexstore.domain.email.model.AbandonedCartReminderEmail
import org.flexstore.domain.email.model.EmailVerificationLink
import org.flexstore.domain.email.model.OrderConfirmationEmail
import org.flexstore.domain.email.model.OrderStatusEmail
import org.flexstore.domain.email.model.PasswordResetEmail
import org.flexstore.domain.email.model.RestockEmail
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

// Keeps the emails instead of sending or logging them, so that the tests can read them
class RecordingEmailSender : EmailSender {

    private val sentEmails = mutableListOf<OrderConfirmationEmail>()
    private val sentVerificationLinks = mutableListOf<EmailVerificationLink>()
    private val sentPasswordResetEmails = mutableListOf<PasswordResetEmail>()
    private val sentOrderStatusEmails = mutableListOf<OrderStatusEmail>()
    private val sentAbandonedCartReminderEmails = mutableListOf<AbandonedCartReminderEmail>()
    private val sentRestockEmails = mutableListOf<RestockEmail>()

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

    override fun send(email: AbandonedCartReminderEmail) {
        sentAbandonedCartReminderEmails.add(email)
    }

    override fun send(email: RestockEmail) {
        sentRestockEmails.add(email)
    }

    fun sentEmails(): List<OrderConfirmationEmail> = sentEmails.toList()

    fun sentVerificationLinks(): List<EmailVerificationLink> = sentVerificationLinks.toList()

    fun sentPasswordResetEmails(): List<PasswordResetEmail> = sentPasswordResetEmails.toList()

    fun sentOrderStatusEmails(): List<OrderStatusEmail> = sentOrderStatusEmails.toList()

    fun sentAbandonedCartReminderEmails(): List<AbandonedCartReminderEmail> =
        sentAbandonedCartReminderEmails.toList()

    fun sentRestockEmails(): List<RestockEmail> = sentRestockEmails.toList()

    fun clear() {
        sentEmails.clear()
        sentVerificationLinks.clear()
        sentPasswordResetEmails.clear()
        sentOrderStatusEmails.clear()
        sentAbandonedCartReminderEmails.clear()
        sentRestockEmails.clear()
    }
}

// Replaces the sender of the emails only: the verifications are still kept in PostgreSQL
@TestConfiguration
open class RecordingEmailConfig {
    @Bean
    @Primary
    open fun recordingEmailSender() = RecordingEmailSender()
}
