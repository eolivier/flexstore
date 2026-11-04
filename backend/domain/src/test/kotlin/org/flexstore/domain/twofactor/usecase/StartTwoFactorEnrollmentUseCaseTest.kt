package org.flexstore.domain.twofactor.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.twofactor.TwoFactorAuthenticationAlreadyEnabledException
import org.flexstore.domain.twofactor.TwoFactorAuthenticationForbiddenException
import org.flexstore.domain.twofactor.model.TwoFactorAuthentication
import org.flexstore.domain.twofactor.model.TwoFactorSecret
import org.flexstore.domain.user.model.*
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test

class StartTwoFactorEnrollmentUseCaseTest : WithAssertions {

    private val userRepository = InMemoryUserRepository()
    private val useCase = StartTwoFactorEnrollmentUseCase(userRepository)
    private val administrator = User.DefinedUser(
        UserId.ValidUserId("admin-1"), FullName(FirstName("Admin"), LastName("Flexstore")),
        Email("admin@example.com"), PlainPassword("admin-password"), Role.ADMIN
    )
    private val jane = User.DefinedUser(
        UserId.ValidUserId("jane-1"), FullName(FirstName("Jane"), LastName("Doe")),
        Email("jane@example.com"), PlainPassword("password123")
    )
    private val secret = TwoFactorSecret.next()

    init {
        listOf(administrator, jane).forEach { userRepository.save(it) }
    }

    private fun storedTwoFactorAuthenticationOf(userId: UserId) =
        (userRepository.findById(userId) as User.DefinedUser).twoFactorAuthentication

    @Test
    fun `an administrator should start an enrollment kept pending until it is confirmed`() {
        // Act
        useCase.unfold(TwoFactorEnrollmentStart(administrator.id, secret))
        // Assert
        val expected = TwoFactorAuthentication.PendingConfirmation(secret)
        assertThat(storedTwoFactorAuthenticationOf(administrator.id)).isEqualTo(expected)
    }

    @Test
    fun `a customer should not start an enrollment`() {
        // Act
        val thrown = catchThrowable { useCase.unfold(TwoFactorEnrollmentStart(jane.id, secret)) }
        // Assert
        assertThat(thrown).isInstanceOf(TwoFactorAuthenticationForbiddenException::class.java)
        assertThat(storedTwoFactorAuthenticationOf(jane.id)).isEqualTo(TwoFactorAuthentication.Disabled)
    }

    @Test
    fun `an administrator already enrolled should not start a new enrollment`() {
        // Arrange
        val enabledAdmin = administrator.withTwoFactorAuthenticationEnabled(secret)
        userRepository.save(enabledAdmin)
        // Act
        val thrown = catchThrowable { useCase.unfold(TwoFactorEnrollmentStart(administrator.id, secret)) }
        // Assert
        assertThat(thrown).isInstanceOf(TwoFactorAuthenticationAlreadyEnabledException::class.java)
    }
}
