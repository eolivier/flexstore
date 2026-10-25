package org.flexstore.infra.spring.config

import org.assertj.core.api.Assertions.assertThat
import org.flexstore.infra.spring.adapter.email.EmailSender
import org.flexstore.infra.spring.adapter.email.LoggingEmailSender
import org.flexstore.infra.spring.adapter.email.SmtpEmailSender
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class EmailConfigTest {

    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(MailSenderAutoConfiguration::class.java))
        .withUserConfiguration(ShopConfig::class.java, EmailConfig::class.java)
        .withPropertyValues("flexstore.mail.from=Flexstore <no-reply@flexstore.com>", "flexstore.front-url=http://localhost:5173")

    @ParameterizedTest(name = "spring.mail.host = \"{0}\" -> {1}")
    @CsvSource("smtp-relay.example.com, SMTP", "'', logs", "'  ', logs")
    fun `the mail host chooses between sending with SMTP and writing the emails to the logs`(host: String, expected: String) {
        // Arrange
        val expectedAdapter = if (expected == "SMTP") SmtpEmailSender::class.java else LoggingEmailSender::class.java
        // Act
        contextRunner.withPropertyValues("spring.mail.host=$host").run { context ->
            // Assert
            assertThat(context).hasSingleBean(EmailSender::class.java)
            assertThat(context.getBean(EmailSender::class.java)).isInstanceOf(expectedAdapter)
        }
    }
}
