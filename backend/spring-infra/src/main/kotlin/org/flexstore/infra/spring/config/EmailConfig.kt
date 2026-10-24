package org.flexstore.infra.spring.config

import org.flexstore.domain.order.*
import org.flexstore.infra.spring.adapter.email.LoggingEmailRepositoryAdapter
import org.flexstore.infra.spring.adapter.email.SmtpEmailRepositoryAdapter
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
    open fun smtpEmailRepository(mailSender: JavaMailSender): EmailRepository =
        SmtpEmailRepositoryAdapter(mailSender, sender, frontUrl)

    @Bean
    @ConditionalOnExpression("'\${spring.mail.host:}'.isBlank()")
    open fun loggingEmailRepository(): EmailRepository = LoggingEmailRepositoryAdapter(frontUrl)
}
