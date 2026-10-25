package org.flexstore.domain.email

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.user.model.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource
import java.time.Duration
import java.time.Instant

class EmailVerificationTest : WithAssertions {

    private val requestedAt = Instant.parse("2026-10-24T10:00:00Z")
    private val userId = UserId.ValidUserId("u-1")

    private fun userWith(status: AccountStatus) =
        User.DefinedUser(userId, FullName(FirstName("Jane"), LastName("Doe")), Email("jane@example.com"), PlainPassword("password"), status = status)

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
    fun `confirming the email should only activate an account that waits for it`(
        status: AccountStatus,
        expected: AccountStatus
    ) {
        // Arrange
        val verification = EmailVerification.requestedAt(userId, requestedAt)
        // Act
        val confirmed = verification.confirm(userWith(status))
        // Assert
        assertThat(confirmed.status).isEqualTo(expected)
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(AccountStatus::class)
    fun `only an account that waits for the verification of its email should need a link`(
        status: AccountStatus
    ) {
        // Act
        val needed = EmailVerification.isNeededFor(userWith(status))
        // Assert
        assertThat(needed).isEqualTo(status == AccountStatus.PENDING_VERIFICATION)
    }

    @ParameterizedTest(name = "page \"{0}\"")
    @ValueSource(strings = ["https://shop.example/verify-email", "https://shop.example/verify-email/"])
    fun `the link should open the verification page with the token`(page: String) {
        // Arrange
        val email = EmailVerificationLink(
            Email("jane@example.com"), FullName(FirstName("Jane"), LastName("Doe")), VerificationPage(page),
            VerificationToken("abc123"), ExpirationDate(requestedAt)
        )
        // Act
        val link = email.link()
        // Assert
        assertThat(link).isEqualTo(Link("https://shop.example/verify-email?token=abc123"))
    }
}
