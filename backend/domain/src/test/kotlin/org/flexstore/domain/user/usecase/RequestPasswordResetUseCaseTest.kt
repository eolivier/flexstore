package org.flexstore.domain.user.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.email.PasswordReset
import org.flexstore.domain.user.model.*
import org.flexstore.infra.repository.InMemoryEmailRepository
import org.flexstore.infra.repository.InMemoryPasswordResetRepository
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test
import java.time.Instant

class RequestPasswordResetUseCaseTest : WithAssertions {

    private val requestedAt = Instant.parse("2026-10-25T10:00:00Z")
    private val email = Email("jane@example.com")
    private val user = User.DefinedUser(
        UserId.ValidUserId("jane"), FullName(FirstName("Jane"), LastName("Doe")), email, PlainPassword("password123")
    )
    private val userRepository = InMemoryUserRepository().apply { save(user) }
    private val passwordResetRepository = InMemoryPasswordResetRepository()
    private val emailRepository = InMemoryEmailRepository()
    private val requestPasswordResetUseCase =
        RequestPasswordResetUseCase(userRepository, passwordResetRepository, emailRepository)

    @Test
    fun `a customer should receive a link valid for 1 hour`() {
        // Act
        requestPasswordResetUseCase.unfold(PasswordResetRequest(email, requestedAt))
        // Assert
        val sentEmail = emailRepository.sentPasswordResetEmails().single()
        assertThat(sentEmail.recipient).isEqualTo(email)
        val expiringToken = sentEmail.reset.expiringToken
        assertThat(expiringToken.expiresAt.value).isEqualTo(requestedAt + PasswordReset.VALIDITY)
        assertThat(passwordResetRepository.findBy(expiringToken.token)).isEqualTo(sentEmail.reset)
    }

    @Test
    fun `a new link should replace the previous one`() {
        // Arrange
        requestPasswordResetUseCase.unfold(PasswordResetRequest(email, requestedAt))
        val previousToken = emailRepository.sentPasswordResetEmails().single().reset.expiringToken.token
        // Act
        requestPasswordResetUseCase.unfold(PasswordResetRequest(email, requestedAt.plusSeconds(60)))
        // Assert
        assertThat(passwordResetRepository.isUnknown(previousToken)).isTrue()
    }

    @Test
    fun `an unknown email should receive no link, without failing`() {
        // Arrange
        val unknownEmail = Email("nobody@example.com")
        // Act
        val thrown = catchThrowable {
            requestPasswordResetUseCase.unfold(PasswordResetRequest(unknownEmail, requestedAt))
        }
        // Assert
        assertThat(thrown).isNull()
        assertThat(emailRepository.sentPasswordResetEmails()).isEmpty()
    }
}
