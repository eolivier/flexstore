package org.flexstore.infra.spring.adapter.email

import org.assertj.core.api.Assertions.assertThat
import org.flexstore.domain.user.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.junit.jupiter.api.Test
import java.time.Instant

class EmailVerificationEmailContentTest {

    private val email = EmailVerificationEmail(
        Email("jane@example.com"),
        FullName(FirstName("Jane"), LastName("Doe")),
        VerificationToken("abc123"),
        ExpirationDate(Instant.parse("2026-10-24T11:00:00Z"))
    )

    @ParameterizedTest(name = "front url \"{0}\"")
    @ValueSource(strings = ["https://flexstore.example", "https://flexstore.example/"])
    fun `the link should open the verification page of the front with the token`(frontUrl: String) {
        // Act
        val content = EmailVerificationEmailContent(email, frontUrl)
        // Assert
        assertThat(content.link).isEqualTo("https://flexstore.example/verify-email?token=abc123")
        assertThat(content.body).contains(content.link)
    }

    @Test
    fun `the email should greet the customer and give the end of validity of the link, in Paris time`() {
        // Act
        val content = EmailVerificationEmailContent(email, "https://flexstore.example")
        // Assert
        assertThat(content.body).startsWith("Bonjour Jane Doe,")
        assertThat(content.body).contains("valable 1 heure (jusqu'à 13h00)")
    }
}
