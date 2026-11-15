package org.flexstore.domain.user.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.user.model.*
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class SignInWithGoogleUseCaseTest : WithAssertions {

    private val userRepository = InMemoryUserRepository()
    private val janeEmail = Email("jane@example.com")
    private val janeDoe = FullName(FirstName("Jane"), LastName("Doe"))
    private val jane = GoogleIdentity(janeDoe, janeEmail)
    private val signInWithGoogleUseCase = SignInWithGoogleUseCase(userRepository)

    private fun existingJane(status: AccountStatus) = User.DefinedUser(
        UserId.ValidUserId("jane-1"), janeDoe, janeEmail, PlainPassword("password123"), status = status
    ).also { userRepository.save(it) }

    // The in-memory repository already holds some users: only the accounts of Jane are counted
    private fun accountsOfJane() =
        userRepository.findAll().filterIsInstance<User.DefinedUser>().filter { it.email == janeEmail }

    @Test
    fun `a customer who signs in with Google for the first time should get an active account`() {
        // Arrange
        // Act
        signInWithGoogleUseCase.unfold(jane)
        // Assert
        val customer = userRepository.findByEmail(janeEmail) as User.DefinedUser
        assertThat(customer.email).isEqualTo(janeEmail)
        assertThat(customer.fullName).isEqualTo(janeDoe)
        assertThat(customer.role).isEqualTo(Role.USER)
        assertThat(customer.status).isEqualTo(AccountStatus.ACTIVE)
    }

    @Test
    fun `a customer who already has an account should sign in to it, without a second account`() {
        // Arrange
        val existingAccount = existingJane(AccountStatus.ACTIVE)
        // Act
        signInWithGoogleUseCase.unfold(jane)
        // Assert
        assertThat((userRepository.findByEmail(janeEmail) as User.DefinedUser).id).isEqualTo(existingAccount.id)
        assertThat(accountsOfJane()).hasSize(1)
    }

    @Test
    fun `a customer whose account waits for the verification of their email should sign in`() {
        // Arrange
        existingJane(AccountStatus.PENDING_VERIFICATION)
        // Act
        signInWithGoogleUseCase.unfold(jane)
        // Assert
        assertThat(userRepository.findByEmail(janeEmail)).isInstanceOf(User.DefinedUser::class.java)
    }

    @ParameterizedTest(name = "an account {0} should not sign in with Google")
    @EnumSource(value = AccountStatus::class, names = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `a suspended, locked or disabled account should not sign in with Google`(status: AccountStatus) {
        // Arrange
        existingJane(status)
        // Act
        val thrown = catchThrowable { signInWithGoogleUseCase.unfold(jane) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(AccountSignInRefusedException::class.java)
        assertThat((thrown.cause as AccountSignInRefusedException).status).isEqualTo(status)
    }
}
