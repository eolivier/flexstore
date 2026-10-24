package org.flexstore.domain.user

import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import java.time.Duration
import java.time.Instant

class EmailVerificationTest : WithAssertions {

    private val requestedAt = Instant.parse("2026-10-24T10:00:00Z")
    private val userId = UserId.ValidUserId("u-1")

    @Test
    fun `a verification link should be valid for 1 hour`() {
        // Act
        val verification = EmailVerification.requestedAt(userId, requestedAt)
        // Assert
        assertThat(verification.expiresAt).isEqualTo(ExpirationDate(Instant.parse("2026-10-24T11:00:00Z")))
    }

    @ParameterizedTest(name = "{0} minutes after the request: expired = {1}")
    @CsvSource("0, false", "59, false", "60, true", "61, true")
    fun `a verification link should expire after 1 hour`(minutes: Long, expired: Boolean) {
        // Arrange
        val verification = EmailVerification.requestedAt(userId, requestedAt)
        // Act
        val isExpired = verification.isExpiredAt(requestedAt + Duration.ofMinutes(minutes))
        // Assert
        assertThat(isExpired).isEqualTo(expired)
    }

    @Test
    fun `each verification link should have its own token`() {
        // Act
        val tokens = (1..100).map { VerificationToken.nextToken() }
        // Assert
        assertThat(tokens).doesNotHaveDuplicates()
        assertThat(tokens.first().value).hasSizeGreaterThanOrEqualTo(43)
    }

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource(
        "PENDING_VERIFICATION, ACTIVE",
        "ACTIVE, ACTIVE",
        "SUSPENDED, SUSPENDED",
        "LOCKED, LOCKED",
        "DISABLED, DISABLED",
    )
    fun `verifying the email should only activate an account that waits for it`(status: AccountStatus, expected: AccountStatus) {
        // Arrange
        val user = User.DefinedUser(userId, FullName(FirstName("Jane"), LastName("Doe")), Email("jane@example.com"), PlainPassword("password"), status = status)
        // Act
        val verified = user.withEmailVerified()
        // Assert
        assertThat(verified.status).isEqualTo(expected)
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(AccountStatus::class)
    fun `only an account that waits for the verification should have an unverified email`(status: AccountStatus) {
        // Arrange
        val user = User.DefinedUser(userId, FullName(FirstName("Jane"), LastName("Doe")), Email("jane@example.com"), PlainPassword("password"), status = status)
        // Act
        val verified = user.hasVerifiedEmail()
        // Assert
        assertThat(verified).isEqualTo(status != AccountStatus.PENDING_VERIFICATION)
    }
}
