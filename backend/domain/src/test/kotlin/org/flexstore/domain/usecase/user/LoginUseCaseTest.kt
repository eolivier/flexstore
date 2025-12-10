package org.flexstore.domain.usecase.user

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.flexstore.domain.entity.*
import org.flexstore.domain.entity.User.DefinedUser
import org.flexstore.domain.entity.UserId.ValidUserId
import org.flexstore.domain.port.PasswordEncoder
import org.flexstore.domain.repository.UserRepository
import org.flexstore.domain.valueobject.Name
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class LoginUseCaseTest {

    @Test
    fun `should authenticate user successfully with valid credentials`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val passwordEncoder = mockk<PasswordEncoder>()
        val email = Email("john.doe@example.com")
        val rawPassword = "password123"
        val hashedPassword = "hashedPassword123"
        val user = DefinedUser(ValidUserId("123"), Name("John Doe"), email, Password(hashedPassword))
        val loginRequest = LoginRequest(email, Password(rawPassword))
        
        every { userRepository.findByEmail(email) } returns user
        every { passwordEncoder.matches(rawPassword, hashedPassword) } returns true
        
        val loginUseCase = LoginUseCase(userRepository, passwordEncoder)
        
        // Act
        loginUseCase.unfold(loginRequest)
        
        // Assert
        val authenticatedUser = loginUseCase.authenticatedUser
        assertThat(authenticatedUser).isEqualTo(user)
        verify { userRepository.findByEmail(email) }
        verify { passwordEncoder.matches(rawPassword, hashedPassword) }
    }

    @Test
    fun `should fail when user does not exist`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val passwordEncoder = mockk<PasswordEncoder>()
        val email = Email("nonexistent@example.com")
        val password = Password("password123")
        val loginRequest = LoginRequest(email, password)
        
        every { userRepository.findByEmail(email) } returns User.UndefinedUser(
            UserId.InvalidUserId(""), 
            "User not found"
        )
        
        val loginUseCase = LoginUseCase(userRepository, passwordEncoder)
        
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
        val passwordEncoder = mockk<PasswordEncoder>()
        val email = Email("john.doe@example.com")
        val correctPasswordHash = "hashedPassword123"
        val incorrectPassword = "wrongpassword"
        val user = DefinedUser(ValidUserId("123"), Name("John Doe"), email, Password(correctPasswordHash))
        val loginRequest = LoginRequest(email, Password(incorrectPassword))
        
        every { userRepository.findByEmail(email) } returns user
        every { passwordEncoder.matches(incorrectPassword, correctPasswordHash) } returns false
        
        val loginUseCase = LoginUseCase(userRepository, passwordEncoder)
        
        // Act & Assert
        val exception = assertThrows<InvalidCredentialsException> {
            loginUseCase.unfold(loginRequest)
        }
        assertThat(exception.message).isEqualTo("Invalid credentials.")
    }
}
