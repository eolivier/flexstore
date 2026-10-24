package org.flexstore.domain.user.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.user.*
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class LoginAccountStatusTest : WithAssertions {

    private val userRepository = InMemoryUserRepository()
    private val loginUseCase = LoginUseCase(userRepository)
    private val email = Email("jane@example.com")

    private fun janeWith(status: AccountStatus) {
        userRepository.save(User.DefinedUser(UserId.ValidUserId("jane-1"), FullName(FirstName("Jane"), LastName("Doe")), email, PlainPassword("password123"), status = status))
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(AccountStatus::class, names = ["PENDING_VERIFICATION", "ACTIVE"])
    fun `an account waiting for the verification or active should sign in`(status: AccountStatus) {
        // Arrange
        janeWith(status)
        // Act
        loginUseCase.unfold(LoginRequest(email, PlainPassword("password123")))
        // Assert
        assertThat((loginUseCase.authenticatedUser as User.DefinedUser).status).isEqualTo(status)
    }

    @ParameterizedTest(name = "{0}")
    @EnumSource(AccountStatus::class, names = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `a suspended, locked or disabled account should not sign in`(status: AccountStatus) {
        // Arrange
        janeWith(status)
        // Act
        val thrown = catchThrowable { loginUseCase.unfold(LoginRequest(email, PlainPassword("password123"))) }
        // Assert
        assertThat(thrown).isInstanceOf(AccountSignInRefusedException::class.java)
        assertThat((thrown as AccountSignInRefusedException).status).isEqualTo(status)
    }

    @Test
    fun `a wrong password should not reveal the status of the account`() {
        // Arrange
        janeWith(AccountStatus.SUSPENDED)
        // Act
        val thrown = catchThrowable { loginUseCase.unfold(LoginRequest(email, PlainPassword("wrong-password"))) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidCredentialsException::class.java)
    }
}
