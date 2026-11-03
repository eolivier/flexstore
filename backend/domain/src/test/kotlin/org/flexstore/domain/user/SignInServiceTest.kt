package org.flexstore.domain.user

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.usecase.*
import org.flexstore.infra.repository.InMemoryFailedSignInRepository
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class SignInServiceTest : WithAssertions {

    private val userRepository = InMemoryUserRepository()
    private val failedSignInRepository = InMemoryFailedSignInRepository()
    private val signInService = SignInService(
        userRepository,
        RecordFailedSignInUseCase(userRepository, failedSignInRepository),
        ResetFailedSignInsUseCase(failedSignInRepository)
    )
    private val janeId = UserId.ValidUserId("jane-1")
    private val janeEmail = Email("jane@example.com")
    private val rightPassword = PlainPassword("password123")
    private val wrongPassword = PlainPassword("wrong-password")

    private fun janeWith(status: AccountStatus) {
        val janeDoe = FullName(FirstName("Jane"), LastName("Doe"))
        userRepository.save(User.DefinedUser(janeId, janeDoe, janeEmail, rightPassword, status = status))
    }

    private fun signInWrongly(times: Int) = repeat(times) {
        catchThrowable { signInService.signIn(LoginRequest(janeEmail, wrongPassword)) }
    }

    private fun failedSignInsOfJane() = failedSignInRepository.countOf(janeId)

    private fun statusOfJane() = (userRepository.findByEmail(janeEmail) as User.DefinedUser).status

    @Test
    fun `an account below the limit of failed sign-ins should stay active`() {
        // Arrange
        janeWith(AccountStatus.ACTIVE)
        // Act
        signInWrongly(FailedSignInCount.LIMIT - 1)
        // Assert
        assertThat(statusOfJane()).isEqualTo(AccountStatus.ACTIVE)
        assertThat(failedSignInsOfJane()).isEqualTo(FailedSignInCount(FailedSignInCount.LIMIT - 1))
    }

    @Test
    fun `an account that reaches the limit of failed sign-ins should be locked`() {
        // Arrange
        janeWith(AccountStatus.ACTIVE)
        // Act
        signInWrongly(FailedSignInCount.LIMIT)
        // Assert
        assertThat(statusOfJane()).isEqualTo(AccountStatus.LOCKED)
        assertThat(failedSignInsOfJane()).isEqualTo(FailedSignInCount.none())
    }

    @Test
    fun `an account reactivated by an administrator should be counted again from none`() {
        // Arrange
        janeWith(AccountStatus.ACTIVE)
        signInWrongly(FailedSignInCount.LIMIT)
        janeWith(AccountStatus.ACTIVE)
        // Act
        signInWrongly(1)
        // Assert
        assertThat(statusOfJane()).isEqualTo(AccountStatus.ACTIVE)
        assertThat(failedSignInsOfJane()).isEqualTo(FailedSignInCount(1))
    }

    @Test
    fun `a locked account should not sign in, even with the right password`() {
        // Arrange
        janeWith(AccountStatus.ACTIVE)
        signInWrongly(FailedSignInCount.LIMIT)
        // Act
        val thrown = catchThrowable { signInService.signIn(LoginRequest(janeEmail, rightPassword)) }
        // Assert
        assertThat(thrown).isInstanceOf(AccountSignInRefusedException::class.java)
        assertThat((thrown as AccountSignInRefusedException).status).isEqualTo(AccountStatus.LOCKED)
    }

    @Test
    fun `an account waiting for the verification should be locked at the limit too`() {
        // Arrange
        janeWith(AccountStatus.PENDING_VERIFICATION)
        // Act
        signInWrongly(FailedSignInCount.LIMIT)
        // Assert
        assertThat(statusOfJane()).isEqualTo(AccountStatus.LOCKED)
    }

    @Test
    fun `a wrong password should still be told as invalid credentials`() {
        // Arrange
        janeWith(AccountStatus.ACTIVE)
        // Act
        val thrown = catchThrowable { signInService.signIn(LoginRequest(janeEmail, wrongPassword)) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidCredentialsException::class.java)
    }

    @Test
    fun `a successful sign-in should start the count again`() {
        // Arrange
        janeWith(AccountStatus.ACTIVE)
        signInWrongly(FailedSignInCount.LIMIT - 1)
        signInService.signIn(LoginRequest(janeEmail, rightPassword))
        // Act
        signInWrongly(FailedSignInCount.LIMIT - 1)
        // Assert
        assertThat(statusOfJane()).isEqualTo(AccountStatus.ACTIVE)
        assertThat(failedSignInsOfJane()).isEqualTo(FailedSignInCount(FailedSignInCount.LIMIT - 1))
    }

    @Test
    fun `a successful sign-in should give the account back`() {
        // Arrange
        janeWith(AccountStatus.ACTIVE)
        // Act
        val user = signInService.signIn(LoginRequest(janeEmail, rightPassword))
        // Assert
        assertThat(user.id).isEqualTo(janeId)
        assertThat(failedSignInsOfJane()).isEqualTo(FailedSignInCount.none())
    }

    @Test
    fun `failed sign-ins of an unknown email should count for nothing`() {
        // Arrange
        janeWith(AccountStatus.ACTIVE)
        val unknownEmail = Email("nobody@example.com")
        // Act
        val thrown = catchThrowable { signInService.signIn(LoginRequest(unknownEmail, wrongPassword)) }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundByEmailException::class.java)
        assertThat(failedSignInsOfJane()).isEqualTo(FailedSignInCount.none())
        assertThat(statusOfJane()).isEqualTo(AccountStatus.ACTIVE)
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(AccountStatus::class, names = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `an account that cannot sign in already should keep its status`(status: AccountStatus) {
        // Arrange
        janeWith(status)
        // Act
        signInWrongly(FailedSignInCount.LIMIT + 1)
        // Assert
        assertThat(statusOfJane()).isEqualTo(status)
        assertThat(failedSignInsOfJane()).isEqualTo(FailedSignInCount.none())
    }
}
