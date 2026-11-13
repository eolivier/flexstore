package org.flexstore.domain.twofactor.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.twofactor.model.TwoFactorCodeInvalidException
import org.flexstore.domain.twofactor.model.TwoFactorEnrollmentNotPendingException
import org.flexstore.domain.twofactor.model.TwoFactorAuthentication
import org.flexstore.domain.twofactor.model.TwoFactorCode
import org.flexstore.domain.twofactor.model.TwoFactorSecret
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.unknownCustomerId
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test
import java.time.Instant

class ConfirmTwoFactorEnrollmentUseCaseTest : WithAssertions {

    private val userRepository = InMemoryUserRepository()
    private val useCase = ConfirmTwoFactorEnrollmentUseCase(userRepository)
    private val administrator = User.DefinedUser(
        UserId.ValidUserId("admin-1"), FullName(FirstName("Admin"), LastName("Flexstore")),
        Email("admin@example.com"), PlainPassword("admin-password"), Role.ADMIN
    )
    private val secret = TwoFactorSecret.next()

    init {
        userRepository.save(administrator.withTwoFactorAuthenticationPending(secret))
    }

    private fun storedTwoFactorAuthenticationOf(userId: UserId) =
        (userRepository.findById(userId) as User.DefinedUser).twoFactorAuthentication

    @Test
    fun `the right code should enable the pending enrollment`() {
        // Arrange
        val code = secret.codeAt(Instant.now())
        // Act
        useCase.unfold(TwoFactorEnrollmentConfirmation(administrator.id, code))
        // Assert
        val expected = TwoFactorAuthentication.Enabled(secret)
        assertThat(storedTwoFactorAuthenticationOf(administrator.id)).isEqualTo(expected)
    }

    @Test
    fun `a wrong code should not enable the pending enrollment`() {
        // Act
        val thrown = catchThrowable {
            useCase.unfold(TwoFactorEnrollmentConfirmation(administrator.id, TwoFactorCode("000000")))
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(TwoFactorCodeInvalidException::class.java)
        assertThat(storedTwoFactorAuthenticationOf(administrator.id)).isEqualTo(
            TwoFactorAuthentication.PendingConfirmation(secret)
        )
    }

    @Test
    fun `an administrator without a pending enrollment should not confirm one`() {
        // Arrange
        val other = User.DefinedUser(
            UserId.ValidUserId("admin-2"), FullName(FirstName("Other"), LastName("Admin")),
            Email("other@example.com"), PlainPassword("password123"), Role.ADMIN
        )
        userRepository.save(other)
        // Act
        val thrown = catchThrowable {
            useCase.unfold(TwoFactorEnrollmentConfirmation(other.id, secret.codeAt(Instant.now())))
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(TwoFactorEnrollmentNotPendingException::class.java)
    }

    @Test
    fun `a customer should confirm their pending enrollment`() {
        // Arrange
        val jane = User.DefinedUser(
            UserId.ValidUserId("jane-1"), FullName(FirstName("Jane"), LastName("Doe")),
            Email("jane@example.com"), PlainPassword("password123")
        )
        userRepository.save(jane.withTwoFactorAuthenticationPending(secret))
        // Act
        useCase.unfold(TwoFactorEnrollmentConfirmation(jane.id, secret.codeAt(Instant.now())))
        // Assert
        assertThat(storedTwoFactorAuthenticationOf(jane.id)).isEqualTo(TwoFactorAuthentication.Enabled(secret))
    }

    @Test
    fun `an unknown user should not confirm an enrollment`() {
        // Act
        val thrown = catchThrowable {
            useCase.unfold(TwoFactorEnrollmentConfirmation(unknownCustomerId, secret.codeAt(Instant.now())))
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserNotFoundException::class.java)
    }
}
