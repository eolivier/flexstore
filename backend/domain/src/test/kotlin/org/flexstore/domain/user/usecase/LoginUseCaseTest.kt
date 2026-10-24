package org.flexstore.domain.user.usecase

import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.*
import org.flexstore.domain.user.User.DefinedUser
import org.flexstore.domain.user.UserId.ValidUserId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import kotlin.test.Test

class LoginUseCaseTest {

    private val userRepository = mockk<UserRepository>()
    private val loginUseCase = LoginUseCase(userRepository)
    private val email = Email("john.doe@example.com")
    private val user = DefinedUser(ValidUserId("123"), FirstName("John"), LastName("Doe"), email, HashedPassword("hashedPassword123"))

    @Test
    fun `should authenticate user successfully with valid credentials`() {
        // Arrange
        val password = PlainPassword("password123")
        every { userRepository.findByEmail(email) } returns user
        every { userRepository.passwordDoesNotMatch(email, password) } returns false
        // Act
        loginUseCase.unfold(LoginRequest(email, password))
        // Assert
        assertThat(loginUseCase.authenticatedUser).isEqualTo(user)
        verify { userRepository.passwordDoesNotMatch(email, password) }
    }

    @Test
    fun `should fail when user does not exist`() {
        // Arrange
        val unknownEmail = Email("nonexistent@example.com")
        every { userRepository.findByEmail(unknownEmail) } returns User.UndefinedUser(UserId.InvalidUserId(""), "User not found")
        // Act
        val thrown = catchThrowable { loginUseCase.unfold(LoginRequest(unknownEmail, PlainPassword("password123"))) }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundByEmailException::class.java)
            .hasMessage("User with email nonexistent@example.com does not exist.")
    }

    @Test
    fun `should fail when password is incorrect`() {
        // Arrange
        val incorrectPassword = PlainPassword("wrongpassword")
        every { userRepository.findByEmail(email) } returns user
        every { userRepository.passwordDoesNotMatch(email, incorrectPassword) } returns true
        // Act
        val thrown = catchThrowable { loginUseCase.unfold(LoginRequest(email, incorrectPassword)) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidCredentialsException::class.java).hasMessage("Invalid credentials.")
    }
}
