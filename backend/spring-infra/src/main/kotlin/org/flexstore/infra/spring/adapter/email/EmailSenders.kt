package org.flexstore.infra.spring.adapter.email

import org.flexstore.domain.sharedkernel.ShopUrl
import org.flexstore.domain.email.*
import org.flexstore.domain.user.model.Email
import org.slf4j.LoggerFactory
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender

// How the emails leave the shop: with an SMTP server, or written to the logs (see EmailConfig)
interface EmailSender {
    fun send(email: OrderConfirmationEmail)
    fun send(email: OrderStatusEmail)
    fun send(link: EmailVerificationLink)
    fun send(email: PasswordResetEmail)
}

/**
 * Sends the emails with the SMTP server of spring.mail (FLEXSTORE_MAIL_HOST...).
 * A failure to send is logged and does not
 * fail the action: the order is placed, and the customer can ask for a new verification link from their account.
 */
class SmtpEmailSender(
    private val mailSender: JavaMailSender,
    private val sender: String,
    private val shopUrl: ShopUrl
) : EmailSender {

    private val logger = LoggerFactory.getLogger(SmtpEmailSender::class.java)

    override fun send(email: OrderConfirmationEmail) {
        val content = OrderConfirmationEmailContent(email, shopUrl)
        val orderNumber = email.order.number()
        runCatching { mailSender.send(message(email.recipient, content)) }
            .onSuccess { logger.info("Order confirmation email of order {} sent to {}", orderNumber, email.recipient.value) }
            .onFailure { logger.error("Order confirmation email of order $orderNumber to ${email.recipient.value} not sent", it) }
    }

    override fun send(email: OrderStatusEmail) {
        val content = OrderStatusEmailContent(email, shopUrl)
        val orderNumber = email.order.number()
        val recipient = email.recipient.value
        runCatching { mailSender.send(message(email.recipient, content)) }
            .onSuccess { logger.info("Status email of order {} sent to {}", orderNumber, recipient) }
            .onFailure { logger.error("Status email of order $orderNumber to $recipient not sent", it) }
    }

    override fun send(link: EmailVerificationLink) {
        val content = EmailVerificationLinkContent(link)
        runCatching { mailSender.send(message(link.recipient, content)) }
            .onSuccess { logger.info("Verification email sent to {}", link.recipient.value) }
            .onFailure { logger.error("Verification email to ${link.recipient.value} not sent", it) }
    }

    override fun send(email: PasswordResetEmail) {
        val content = PasswordResetEmailContent(email, shopUrl)
        runCatching { mailSender.send(message(email.recipient, content)) }
            .onSuccess { logger.info("Password reset email sent to {}", email.recipient.value) }
            .onFailure { logger.error("Password reset email to ${email.recipient.value} not sent", it) }
    }

    private fun message(recipient: Email, content: EmailContent) = SimpleMailMessage().apply {
        from = sender
        setTo(recipient.value)
        subject = content.subject.value
        text = content.body.value
    }
}

// Without SMTP server: the emails are written to the logs (local development, tests)
class LoggingEmailSender(private val shopUrl: ShopUrl) : EmailSender {

    private val logger = LoggerFactory.getLogger(LoggingEmailSender::class.java)

    override fun send(email: OrderConfirmationEmail) {
        val content = OrderConfirmationEmailContent(email, shopUrl)
        log(email.recipient, content)
    }

    override fun send(email: OrderStatusEmail) {
        log(email.recipient, OrderStatusEmailContent(email, shopUrl))
    }

    override fun send(link: EmailVerificationLink) {
        val content = EmailVerificationLinkContent(link)
        log(link.recipient, content)
    }

    override fun send(email: PasswordResetEmail) {
        log(email.recipient, PasswordResetEmailContent(email, shopUrl))
    }

    private fun log(recipient: Email, content: EmailContent) = logger.info(
        "Email to {} (no SMTP server configured)\nSubject: {}\n{}",
        recipient.value, content.subject.value, content.body.value
    )
}
