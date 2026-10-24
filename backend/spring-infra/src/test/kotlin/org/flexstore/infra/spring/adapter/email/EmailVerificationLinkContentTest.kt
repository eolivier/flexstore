package org.flexstore.infra.spring.adapter.email

import org.assertj.core.api.Assertions.assertThat
import org.flexstore.domain.email.*
import org.flexstore.domain.user.*
import org.junit.jupiter.api.Test
import java.time.Instant

class EmailVerificationLinkContentTest {

    private val link = EmailVerificationLink(
        Email("jane@example.com"),
        FullName(FirstName("Jane"), LastName("Doe")),
        VerificationPage("https://flexstore.example/verify-email"),
        VerificationToken("abc123"),
        ExpirationDate(Instant.parse("2026-10-24T11:00:00Z"))
    )

    @Test
    fun `the email should give the link of the verification page`() {
        // Act
        val content = EmailVerificationLinkContent(link)
        // Assert
        assertThat(content.body).contains("https://flexstore.example/verify-email?token=abc123")
    }

    @Test
    fun `the email should greet the customer and give the end of validity of the link, in Paris time`() {
        // Act
        val content = EmailVerificationLinkContent(link)
        // Assert
        assertThat(content.body).startsWith("Bonjour Jane Doe,")
        assertThat(content.body).contains("valable 1 heure (jusqu'à 13h00)")
    }
}
