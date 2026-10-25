package org.flexstore.domain.user.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.email.*
import org.flexstore.domain.user.model.*
import org.flexstore.infra.repository.InMemoryPasswordResetRepository
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test
import java.time.Instant

class ResetPasswordUseCaseTest : WithAssertions {

    private val requestedAt = Instant.parse("2026-10-25T10:00:00Z")
    private val email = Email("jane@example.com")
    private val user = User.DefinedUser(
        UserId.ValidUserId("jane"), FullName(FirstName("Jane"), LastName("Doe")), email, PlainPassword("old-password")
    )
    private val reset = PasswordReset.requestedAt(user.id, requestedAt)
    private val token = reset.expiringToken.token
    private val userRepository = InMemoryUserRepository().apply { save(user) }
    private val passwordResetRepository = InMemoryPasswordResetRepository().apply { save(reset) }
    private val resetPasswordUseCase = ResetPasswordUseCase(userRepository, passwordResetRepository)

    private fun confirmation(password: String, confirmedAt: Instant = requestedAt.plusSeconds(600)) =
        PasswordResetConfirmation(ChosenPassword(token, PlainPassword(password)), confirmedAt)

    @Test
    fun `the customer should sign in with the new password`() {
        // Act
        resetPasswordUseCase.unfold(confirmation("new-password"))
        // Assert
        assertThat(userRepository.passwordMatches(email, PlainPassword("new-password"))).isTrue()
        assertThat(userRepository.passwordMatches(email, PlainPassword("old-password"))).isFalse()
    }

    @Test
    fun `the link should not be usable again`() {
        // Arrange
        resetPasswordUseCase.unfold(confirmation("new-password"))
        // Act
        val thrown = catchThrowable { resetPasswordUseCase.unfold(confirmation("another-password")) }
        // Assert
        assertThat(thrown).isInstanceOf(PasswordResetLinkUnknownException::class.java)
    }

    @Test
    fun `a link older than 1 hour should be refused`() {
        // Act
        val thrown = catchThrowable {
            resetPasswordUseCase.unfold(confirmation("new-password", requestedAt + PasswordReset.VALIDITY))
        }
        // Assert
        assertThat(thrown).isInstanceOf(PasswordResetLinkExpiredException::class.java)
        assertThat(userRepository.passwordMatches(email, PlainPassword("old-password"))).isTrue()
    }

    @Test
    fun `a password shorter than 8 characters should be refused`() {
        // Act
        val thrown = catchThrowable { resetPasswordUseCase.unfold(confirmation("short")) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidPasswordException::class.java)
    }
}
