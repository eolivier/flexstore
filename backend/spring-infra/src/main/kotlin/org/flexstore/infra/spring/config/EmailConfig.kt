package org.flexstore.infra.spring.config

import org.flexstore.domain.order.*
import org.flexstore.infra.spring.adapter.email.LoggingEmailRepositoryAdapter
import org.flexstore.infra.spring.adapter.email.SmtpEmailRepositoryAdapter
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.mail.javamail.JavaMailSender

// SMTP when SPRING_MAIL_HOST is set (Spring Boot then configures the JavaMailSender), logs otherwise
@Configuration
open class EmailConfig(
    @Value("\${flexstore.mail.from}") private val sender: String,
    @Value("\${flexstore.front-url}") private val frontUrl: String
) {

    @Bean
    @ConditionalOnProperty("spring.mail.host")
    open fun smtpEmailRepository(mailSender: JavaMailSender): EmailRepository =
        SmtpEmailRepositoryAdapter(mailSender, sender, frontUrl)

    @Bean
    @ConditionalOnExpression("'\${spring.mail.host:}'.isEmpty()")
    open fun loggingEmailRepository(): EmailRepository = LoggingEmailRepositoryAdapter(frontUrl)
}
