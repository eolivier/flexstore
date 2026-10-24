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
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class LoginUseCaseTest {

    @Test
    fun `should authenticate user successfully with valid credentials`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val email = Email("john.doe@example.com")
        val password = PlainPassword("password123")
        val user = DefinedUser(ValidUserId("123"), Name("John Doe"), email, HashedPassword("hashedPassword123"))
        val loginRequest = LoginRequest(email, password)
        
        every { userRepository.findByEmail(email) } returns user
        every { userRepository.passwordMatches(email, password) } returns true
        
        val loginUseCase = LoginUseCase(userRepository)
        
        // Act
        loginUseCase.unfold(loginRequest)
        
        // Assert
        val authenticatedUser = loginUseCase.authenticatedUser
        assertThat(authenticatedUser).isEqualTo(user)
        verify { userRepository.findByEmail(email) }
        verify { userRepository.passwordMatches(email, password) }
    }

    @Test
    fun `should fail when user does not exist`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val email = Email("nonexistent@example.com")
        val password = PlainPassword("password123")
        val loginRequest = LoginRequest(email, password)
        
        every { userRepository.findByEmail(email) } returns User.UndefinedUser(
            UserId.InvalidUserId(""), 
            "User not found"
        )
        
        val loginUseCase = LoginUseCase(userRepository)
        
        // Act & Assert
        val exception = assertThrows<UserNotFoundByEmailException> {
            loginUseCase.unfold(loginRequest)
        }
        assertThat(exception.message).isEqualTo("User with email nonexistent@example.com does not exist.")
    }

    @Test
    fun `should fail when password is incorrect`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val email = Email("john.doe@example.com")
        val incorrectPassword = PlainPassword("wrongpassword")
        val user = DefinedUser(ValidUserId("123"), Name("John Doe"), email, HashedPassword("hashedPassword123"))
        val loginRequest = LoginRequest(email, incorrectPassword)
        
        every { userRepository.findByEmail(email) } returns user
        every { userRepository.passwordMatches(email, incorrectPassword) } returns false
        
        val loginUseCase = LoginUseCase(userRepository)
        
        // Act & Assert
        val exception = assertThrows<InvalidCredentialsException> {
            loginUseCase.unfold(loginRequest)
        }
        assertThat(exception.message).isEqualTo("Invalid credentials.")
    }
}
