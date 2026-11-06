package org.flexstore.domain.user.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.repository.GoogleIdentityVerifier
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.ucop.domain.ErrorMessage

class SignInWithGoogleUseCaseTest : WithAssertions {

    private val userRepository = InMemoryUserRepository()
    private val janeEmail = Email("jane@example.com")
    private val janeDoe = FullName(FirstName("Jane"), LastName("Doe"))
    private val idToken = GoogleIdToken("google-token")
    private val googleKnowsJane = GoogleIdentityVerifier { GoogleIdentity(janeDoe, janeEmail) }
    private val googleRefusesTheToken = GoogleIdentityVerifier {
        throw InvalidGoogleIdTokenException(ErrorMessage("The token is not Google's."))
    }

    private fun existingJane(status: AccountStatus) = User.DefinedUser(
        UserId.ValidUserId("jane-1"), janeDoe, janeEmail, PlainPassword("password123"), status = status
    ).also { userRepository.save(it) }

    // The in-memory repository already holds some users: only the accounts of Jane are counted
    private fun accountsOfJane() =
        userRepository.findAll().filterIsInstance<User.DefinedUser>().filter { it.email == janeEmail }

    @Test
    fun `a customer who signs in with Google for the first time should get an active account`() {
        // Arrange
        val signInWithGoogleUseCase = SignInWithGoogleUseCase(userRepository, googleKnowsJane)
        // Act
        signInWithGoogleUseCase.unfold(idToken)
        // Assert
        val customer = signInWithGoogleUseCase.authenticatedUser as User.DefinedUser
        assertThat(customer.email).isEqualTo(janeEmail)
        assertThat(customer.fullName).isEqualTo(janeDoe)
        assertThat(customer.role).isEqualTo(Role.USER)
        assertThat(customer.status).isEqualTo(AccountStatus.ACTIVE)
    }

    @Test
    fun `a customer who already has an account should sign in to it, without a second account`() {
        // Arrange
        val jane = existingJane(AccountStatus.ACTIVE)
        val signInWithGoogleUseCase = SignInWithGoogleUseCase(userRepository, googleKnowsJane)
        // Act
        signInWithGoogleUseCase.unfold(idToken)
        // Assert
        assertThat((signInWithGoogleUseCase.authenticatedUser as User.DefinedUser).id).isEqualTo(jane.id)
        assertThat(accountsOfJane()).hasSize(1)
    }

    @Test
    fun `a customer whose account waits for the verification of their email should sign in`() {
        // Arrange
        existingJane(AccountStatus.PENDING_VERIFICATION)
        val signInWithGoogleUseCase = SignInWithGoogleUseCase(userRepository, googleKnowsJane)
        // Act
        signInWithGoogleUseCase.unfold(idToken)
        // Assert
        assertThat(signInWithGoogleUseCase.authenticatedUser).isInstanceOf(User.DefinedUser::class.java)
    }

    @ParameterizedTest(name = "an account {0} should not sign in with Google")
    @EnumSource(value = AccountStatus::class, names = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `a suspended, locked or disabled account should not sign in with Google`(status: AccountStatus) {
        // Arrange
        existingJane(status)
        val signInWithGoogleUseCase = SignInWithGoogleUseCase(userRepository, googleKnowsJane)
        // Act
        val thrown = catchThrowable { signInWithGoogleUseCase.unfold(idToken) }
        // Assert
        assertThat(thrown).isInstanceOf(AccountSignInRefusedException::class.java)
        assertThat((thrown as AccountSignInRefusedException).status).isEqualTo(status)
    }

    @Test
    fun `a token that Google does not vouch for should neither sign in nor create an account`() {
        // Arrange
        val signInWithGoogleUseCase = SignInWithGoogleUseCase(userRepository, googleRefusesTheToken)
        // Act
        val thrown = catchThrowable { signInWithGoogleUseCase.unfold(idToken) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidGoogleIdTokenException::class.java)
        assertThat(accountsOfJane()).isEmpty()
    }
}
