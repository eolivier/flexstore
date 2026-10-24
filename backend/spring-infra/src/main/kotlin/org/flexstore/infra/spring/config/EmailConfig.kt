package org.flexstore.infra.spring.config

import org.flexstore.infra.spring.adapter.email.EmailSender
import org.flexstore.infra.spring.adapter.email.LoggingEmailSender
import org.flexstore.infra.spring.adapter.email.SmtpEmailSender
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender

// SMTP when FLEXSTORE_MAIL_HOST (spring.mail.host) is set, logs otherwise
@Configuration
open class EmailConfig(
    @Value("\${flexstore.mail.from}") private val sender: String,
    @Value("\${flexstore.front-url}") private val frontUrl: String
) {

    // spring.mail.host is always defined (application.yml): it is its value that chooses SMTP or the logs
    @Bean
    @ConditionalOnExpression("!'\${spring.mail.host:}'.isBlank()")
    open fun smtpEmailSender(mailSender: JavaMailSender): EmailSender =
        SmtpEmailSender(mailSender, sender, frontUrl)

    @Bean
    @ConditionalOnExpression("'\${spring.mail.host:}'.isBlank()")
    open fun loggingEmailSender(): EmailSender = LoggingEmailSender(frontUrl)
}
