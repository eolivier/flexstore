package org.flexstore.domain.twofactor.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.twofactor.model.TwoFactorAuthenticationAlreadyEnabledException
import org.flexstore.domain.twofactor.model.TwoFactorAuthentication
import org.flexstore.domain.twofactor.model.TwoFactorSecret
import org.flexstore.domain.user.blockedCustomerId
import org.flexstore.domain.user.customerWithStatus
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.unknownCustomerId
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class StartTwoFactorEnrollmentUseCaseTest : WithAssertions {

    private val userRepository = InMemoryUserRepository()
    private val useCase = StartTwoFactorEnrollmentUseCase(userRepository)
    private val actorResolutionService = ActorResolutionService(userRepository)
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

    private fun actorOf(userId: UserId) = actorResolutionService.actorOf(userId)

    private fun storedTwoFactorAuthenticationOf(userId: UserId) =
        (userRepository.findById(userId) as User.DefinedUser).twoFactorAuthentication

    @Test
    fun `an administrator should start an enrollment kept pending until it is confirmed`() {
        // Act
        useCase.unfold(TwoFactorEnrollmentStart(actorOf(administrator.id), secret))
        // Assert
        val expected = TwoFactorAuthentication.PendingConfirmation(secret)
        assertThat(storedTwoFactorAuthenticationOf(administrator.id)).isEqualTo(expected)
    }

    @Test
    fun `a customer should start an enrollment kept pending until it is confirmed`() {
        // Act
        useCase.unfold(TwoFactorEnrollmentStart(actorOf(jane.id), secret))
        // Assert
        val expected = TwoFactorAuthentication.PendingConfirmation(secret)
        assertThat(storedTwoFactorAuthenticationOf(jane.id)).isEqualTo(expected)
    }

    @Test
    fun `an unknown user should not start an enrollment`() {
        // Arrange
        val enrollmentStart = TwoFactorEnrollmentStart(actorOf(unknownCustomerId), secret)
        // Act
        val thrown = catchThrowable { useCase.unfold(enrollmentStart) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserNotFoundException::class.java)
    }

    @ParameterizedTest
    @EnumSource(AccountStatus::class, names = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `a user whose account is refused should not start an enrollment`(status: AccountStatus) {
        // Arrange
        userRepository.save(customerWithStatus(status))
        val enrollmentStart = TwoFactorEnrollmentStart(actorOf(blockedCustomerId), secret)
        // Act
        val thrown = catchThrowable { useCase.unfold(enrollmentStart) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(AccountRefusedException::class.java)
    }

    @Test
    fun `an administrator already enrolled should not start a new enrollment`() {
        // Arrange
        val enabledAdmin = administrator.withTwoFactorAuthenticationEnabled(secret)
        userRepository.save(enabledAdmin)
        val enrollmentStart = TwoFactorEnrollmentStart(actorOf(administrator.id), secret)
        // Act
        val thrown = catchThrowable { useCase.unfold(enrollmentStart) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(TwoFactorAuthenticationAlreadyEnabledException::class.java)
    }
}
