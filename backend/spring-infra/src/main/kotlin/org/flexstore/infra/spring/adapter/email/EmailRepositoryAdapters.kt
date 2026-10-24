package org.flexstore.infra.spring.adapter.email

import org.flexstore.domain.order.*
import org.slf4j.LoggerFactory
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender

/**
 * Sends the emails with the SMTP server of spring.mail (FLEXSTORE_MAIL_HOST...). A failure to send is logged and does not
 * cancel the order: the email is a notification of an order already placed.
 */
class SmtpEmailRepositoryAdapter(
    private val mailSender: JavaMailSender,
    private val sender: String,
    private val frontUrl: String
) : EmailRepository {

    private val logger = LoggerFactory.getLogger(SmtpEmailRepositoryAdapter::class.java)

    override fun send(email: OrderConfirmationEmail) {
        val content = OrderConfirmationEmailContent(email, frontUrl)
        val message = SimpleMailMessage().apply {
            from = sender
            setTo(email.recipient.value)
            subject = content.subject
            text = content.body
        }
        val orderNumber = email.order.number()
        runCatching { mailSender.send(message) }
            .onSuccess { logger.info("Order confirmation email of order {} sent to {}", orderNumber, email.recipient.value) }
            .onFailure { logger.error("Order confirmation email of order $orderNumber to ${email.recipient.value} not sent", it) }
    }
}

// Without SMTP server: the emails are written to the logs (local development, tests)
class LoggingEmailRepositoryAdapter(private val frontUrl: String) : EmailRepository {

    private val logger = LoggerFactory.getLogger(LoggingEmailRepositoryAdapter::class.java)

    override fun send(email: OrderConfirmationEmail) {
        val content = OrderConfirmationEmailContent(email, frontUrl)
        logger.info("Email to {} (no SMTP server configured)\nSubject: {}\n{}", email.recipient.value, content.subject, content.body)
    }
}
