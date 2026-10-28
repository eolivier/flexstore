package org.flexstore.infra.spring.adapter.email

import org.flexstore.domain.email.EmailVerificationLink
import org.flexstore.domain.email.OrderConfirmationEmail
import org.flexstore.domain.email.OrderStatusEmail
import org.flexstore.domain.email.PasswordResetEmail
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

// Keeps the emails instead of sending or logging them, so that the tests can read them
class RecordingEmailSender : EmailSender {

    private val sentEmails = mutableListOf<OrderConfirmationEmail>()
    private val sentVerificationLinks = mutableListOf<EmailVerificationLink>()
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

    fun sentEmails(): List<OrderConfirmationEmail> = sentEmails.toList()

    fun sentVerificationLinks(): List<EmailVerificationLink> = sentVerificationLinks.toList()

    fun sentPasswordResetEmails(): List<PasswordResetEmail> = sentPasswordResetEmails.toList()

    fun sentOrderStatusEmails(): List<OrderStatusEmail> = sentOrderStatusEmails.toList()

    fun clear() {
        sentEmails.clear()
        sentVerificationLinks.clear()
        sentPasswordResetEmails.clear()
        sentOrderStatusEmails.clear()
    }
}

// Replaces the sender of the emails only: the verifications are still kept in PostgreSQL
@TestConfiguration
open class RecordingEmailConfig {
    @Bean
    @Primary
    open fun recordingEmailSender() = RecordingEmailSender()
}
