package org.flexstore.domain.user

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.twofactor.TwoFactorCodeInvalidException
import org.flexstore.domain.twofactor.model.TwoFactorAuthentication
import org.flexstore.domain.twofactor.model.TwoFactorChallengeId
import org.flexstore.domain.twofactor.model.TwoFactorCode
import org.flexstore.domain.twofactor.model.TwoFactorSecret
import org.flexstore.domain.twofactor.usecase.TwoFactorCodeVerification
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.usecase.*
import org.flexstore.infra.repository.InMemoryFailedSignInRepository
import org.flexstore.infra.repository.InMemoryTwoFactorChallengeRepository
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.time.Instant

class SignInServiceTest : WithAssertions {

    private val userRepository = InMemoryUserRepository()
    private val failedSignInRepository = InMemoryFailedSignInRepository()
    private val twoFactorChallengeRepository = InMemoryTwoFactorChallengeRepository()
    private val signInService = SignInService(
        userRepository,
        RecordFailedSignInUseCase(userRepository, failedSignInRepository),
        ResetFailedSignInsUseCase(failedSignInRepository),
        twoFactorChallengeRepository
    )
    private val janeId = UserId.ValidUserId("jane-1")
    private val janeEmail = Email("jane@example.com")
    private val rightPassword = PlainPassword("password123")
    private val wrongPassword = PlainPassword("wrong-password")
    private val secret = TwoFactorSecret.next()

    private fun janeWith(status: AccountStatus) {
        val janeDoe = FullName(FirstName("Jane"), LastName("Doe"))
        userRepository.save(User.DefinedUser(janeId, janeDoe, janeEmail, rightPassword, status = status))
    }

    private fun janeWithTwoFactorEnabled() {
        val janeDoe = FullName(FirstName("Jane"), LastName("Doe"))
        val admin = User.DefinedUser(
            janeId, janeDoe, janeEmail, rightPassword, role = Role.ADMIN,
            twoFactorAuthentication = TwoFactorAuthentication.Enabled(secret)
        )
        userRepository.save(admin)
    }

    private fun signedInOutcome() = signInService.signIn(LoginRequest(janeEmail, rightPassword))

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
        val outcome = signInService.signIn(LoginRequest(janeEmail, rightPassword))
        // Assert
        assertThat(outcome).isInstanceOf(SignInOutcome.SignedIn::class.java)
        assertThat((outcome as SignInOutcome.SignedIn).user.id).isEqualTo(janeId)
        assertThat(failedSignInsOfJane()).isEqualTo(FailedSignInCount.none())
    }

    @Test
    fun `an administrator with two-factor authentication enabled should be given a challenge, not a session`() {
        // Arrange
        janeWithTwoFactorEnabled()
        // Act
        val outcome = signInService.signIn(LoginRequest(janeEmail, rightPassword))
        // Assert
        assertThat(outcome).isInstanceOf(SignInOutcome.TwoFactorChallengeRequired::class.java)
        assertThat(failedSignInsOfJane()).isEqualTo(FailedSignInCount.none())
    }

    @Test
    fun `the right two-factor code should complete the sign-in`() {
        // Arrange
        janeWithTwoFactorEnabled()
        val challengeId = challengeIdOf(signedInOutcome())
        val code = secret.codeAt(Instant.now())
        // Act
        val user = signInService.verifyTwoFactorCode(TwoFactorCodeVerification(challengeId, code))
        // Assert
        assertThat(user.id).isEqualTo(janeId)
        assertThat(failedSignInsOfJane()).isEqualTo(FailedSignInCount.none())
    }

    @Test
    fun `a wrong two-factor code should count as a failed sign-in, like a wrong password`() {
        // Arrange
        janeWithTwoFactorEnabled()
        val challengeId = challengeIdOf(signedInOutcome())
        // Act
        val thrown = catchThrowable {
            signInService.verifyTwoFactorCode(TwoFactorCodeVerification(challengeId, TwoFactorCode("000000")))
        }
        // Assert
        assertThat(thrown).isInstanceOf(TwoFactorCodeInvalidException::class.java)
        assertThat(failedSignInsOfJane()).isEqualTo(FailedSignInCount(1))
    }

    @Test
    fun `wrong two-factor codes reaching the limit should lock the account, like wrong passwords`() {
        // Arrange
        janeWithTwoFactorEnabled()
        val challengeId = challengeIdOf(signedInOutcome())
        // Act
        val wrongCode = TwoFactorCodeVerification(challengeId, TwoFactorCode("000000"))
        repeat(FailedSignInCount.LIMIT) {
            catchThrowable { signInService.verifyTwoFactorCode(wrongCode) }
        }
        // Assert
        assertThat(statusOfJane()).isEqualTo(AccountStatus.LOCKED)
    }

    private fun challengeIdOf(outcome: SignInOutcome): TwoFactorChallengeId =
        (outcome as SignInOutcome.TwoFactorChallengeRequired).challengeId

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
