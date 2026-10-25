package org.flexstore.domain.email

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.sharedkernel.ShopUrl
import org.flexstore.domain.user.model.*
import org.junit.jupiter.api.Test
import java.time.Instant

class PasswordResetEmailContentTest : WithAssertions {

    private val user = User.DefinedUser(
        UserId.ValidUserId("jane"),
        FullName(FirstName("Jane"), LastName("Doe")),
        Email("jane@example.com"),
        PlainPassword("password123")
    )
    private val expiringToken = ExpiringToken(
        VerificationToken("abc123"), ExpirationDate(Instant.parse("2026-10-24T11:00:00Z"))
    )
    private val email = PasswordResetEmail(user, PasswordReset(user.id, expiringToken))

    @Test
    fun `the email should give the link of the page to choose a new password`() {
        // Act
        val content = PasswordResetEmailContent(email, ShopUrl("https://flexstore.example/"))
        // Assert
        assertThat(content.body.value).contains("https://flexstore.example/reset-password?token=abc123")
    }

    @Test
    fun `the email should greet the customer and give the end of validity of the link, in Paris time`() {
        // Act
        val content = PasswordResetEmailContent(email, ShopUrl("https://flexstore.example"))
        // Assert
        assertThat(content.body.value).startsWith("Bonjour Jane Doe,")
        assertThat(content.body.value).contains("valable 1 heure (jusqu'à 13h00)")
    }
}
