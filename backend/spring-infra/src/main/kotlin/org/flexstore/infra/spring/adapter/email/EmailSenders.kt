package org.flexstore.infra.spring.adapter.email

import org.flexstore.domain.email.*
import org.slf4j.LoggerFactory
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender

// How the emails leave the shop: with an SMTP server, or written to the logs (see EmailConfig)
interface EmailSender {
    fun send(email: OrderConfirmationEmail)
    fun send(link: EmailVerificationLink)
}

/**
 * Sends the emails with the SMTP server of spring.mail (FLEXSTORE_MAIL_HOST...). A failure to send is logged and does not
 * fail the action: the order is placed, and the customer can ask for a new verification link from their account.
 */
class SmtpEmailSender(
    private val mailSender: JavaMailSender,
    private val sender: String,
    private val frontUrl: String
) : EmailSender {

    private val logger = LoggerFactory.getLogger(SmtpEmailSender::class.java)

    override fun send(email: OrderConfirmationEmail) {
        val content = OrderConfirmationEmailContent(email, frontUrl)
        val orderNumber = email.order.number()
        runCatching { mailSender.send(message(email.recipient.value, content.subject, content.body)) }
            .onSuccess { logger.info("Order confirmation email of order {} sent to {}", orderNumber, email.recipient.value) }
            .onFailure { logger.error("Order confirmation email of order $orderNumber to ${email.recipient.value} not sent", it) }
    }

    override fun send(link: EmailVerificationLink) {
        val content = EmailVerificationLinkContent(link)
        runCatching { mailSender.send(message(link.recipient.value, content.subject, content.body)) }
            .onSuccess { logger.info("Verification email sent to {}", link.recipient.value) }
            .onFailure { logger.error("Verification email to ${link.recipient.value} not sent", it) }
    }

    private fun message(recipient: String, subject: String, body: String) = SimpleMailMessage().apply {
        from = sender
        setTo(recipient)
        this.subject = subject
        text = body
    }
}

// Without SMTP server: the emails are written to the logs (local development, tests)
class LoggingEmailSender(private val frontUrl: String) : EmailSender {

    private val logger = LoggerFactory.getLogger(LoggingEmailSender::class.java)

    override fun send(email: OrderConfirmationEmail) {
        val content = OrderConfirmationEmailContent(email, frontUrl)
        log(email.recipient.value, content.subject, content.body)
    }

    override fun send(link: EmailVerificationLink) {
        val content = EmailVerificationLinkContent(link)
        log(link.recipient.value, content.subject, content.body)
    }

    private fun log(recipient: String, subject: String, body: String) =
        logger.info("Email to {} (no SMTP server configured)\nSubject: {}\n{}", recipient, subject, body)
}
