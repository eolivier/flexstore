package org.flexstore.domain.twofactor.model

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.user.model.Email
import org.junit.jupiter.api.Test
import java.time.Instant

private const val TIME_STEP_SECONDS = 30L

class TwoFactorSecretTest : WithAssertions {

    private val secret = TwoFactorSecret.next()
    private val now = Instant.parse("2026-01-01T10:00:00Z")

    @Test
    fun `the code computed for a secret should match itself at the same moment`() {
        // Act
        val code = secret.codeAt(now)
        // Assert
        assertThat(secret.matches(code, now)).isTrue()
    }

    @Test
    fun `a code from the previous or the next time step should still match, tolerating the clock drift`() {
        // Arrange
        val code = secret.codeAt(now)
        // Act & Assert
        assertThat(secret.matches(code, now.minusSeconds(TIME_STEP_SECONDS))).isTrue()
        assertThat(secret.matches(code, now.plusSeconds(TIME_STEP_SECONDS))).isTrue()
    }

    @Test
    fun `a code two time steps away should not match`() {
        // Arrange
        val code = secret.codeAt(now)
        // Act
        val matches = secret.matches(code, now.plusSeconds(2 * TIME_STEP_SECONDS))
        // Assert
        assertThat(matches).isFalse()
    }

    @Test
    fun `a code from another secret should not match`() {
        // Arrange
        val anotherSecret = TwoFactorSecret.next()
        val code = anotherSecret.codeAt(now)
        // Act
        val matches = secret.matches(code, now)
        // Assert
        assertThat(matches).isFalse()
    }

    @Test
    fun `a wrong code should not match`() {
        // Act
        val matches = secret.doesNotMatch(TwoFactorCode("000000"), now)
        // Assert
        assertThat(matches).isTrue()
    }

    @Test
    fun `two secrets generated one after the other should not be the same`() {
        // Act
        val anotherSecret = TwoFactorSecret.next()
        // Assert
        assertThat(secret).isNotEqualTo(anotherSecret)
    }

    @Test
    fun `the provisioning URI should name the issuer, the account and carry the secret`() {
        // Act
        val provisioningUri = secret.provisioningUri(Email("jane@example.com"), TwoFactorIssuer.flexstore())
        // Assert
        assertThat(provisioningUri.value).startsWith("otpauth://totp/Flexstore%3Ajane%40example.com?")
        assertThat(provisioningUri.value).contains("secret=${secret.value}")
        assertThat(provisioningUri.value).contains("issuer=Flexstore")
    }
}
