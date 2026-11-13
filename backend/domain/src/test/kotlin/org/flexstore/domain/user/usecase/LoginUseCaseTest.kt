package org.flexstore.domain.user.usecase

import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.model.User.DefinedUser
import org.flexstore.domain.user.model.UserId.ValidUserId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import kotlin.test.Test
import org.flexstore.domain.user.repository.*

class LoginUseCaseTest {

    private val userRepository = mockk<UserRepository>()
    private val loginUseCase = LoginUseCase(userRepository)
    private val email = Email("john.doe@example.com")
    private val user = DefinedUser(
        ValidUserId("123"),
        FullName(FirstName("John"), LastName("Doe")),
        email,
        HashedPassword("hashedPassword123")
    )

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
        every {
            userRepository.findByEmail(unknownEmail)
        } returns User.UndefinedUser(UserId.InvalidUserId(""), "User not found")
        // Act
        val thrown = catchThrowable {
            loginUseCase.unfold(LoginRequest(unknownEmail, PlainPassword("password123")))
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserNotFoundByEmailException::class.java)
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
        assertThat(thrown)
            .hasCauseInstanceOf(InvalidCredentialsException::class.java)
            .hasMessage("Invalid credentials.")
    }
}

class LoginSystemUserTest {

    private val userRepository = mockk<UserRepository>()
    private val loginUseCase = LoginUseCase(userRepository)
    private val systemEmail = Email("system@flexstore.local")
    private val systemUser = DefinedUser(
        SystemUser.id,
        FullName(FirstName("System"), LastName("System")),
        systemEmail,
        HashedPassword(""),
        Role.SYSTEM
    )

    @Test
    fun `should refuse the sign-in of the system user`() {
        // Arrange
        every { userRepository.findByEmail(systemEmail) } returns systemUser
        // Act
        val thrown = catchThrowable { loginUseCase.unfold(LoginRequest(systemEmail, PlainPassword(""))) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(SystemSignInForbiddenException::class.java)
            .hasMessage("The system user cannot sign in.")
    }
}
