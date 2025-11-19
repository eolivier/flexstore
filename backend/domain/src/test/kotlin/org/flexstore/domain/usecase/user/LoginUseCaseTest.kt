package org.flexstore.domain.usecase.user

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.flexstore.domain.entity.*
import org.flexstore.domain.entity.User.DefinedUser
import org.flexstore.domain.entity.UserId.ValidUserId
import org.flexstore.domain.repository.UserRepository
import org.flexstore.domain.valueobject.Name
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class LoginUseCaseTest {

    @Test
    fun `should authenticate user successfully with valid credentials`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val email = Email("john.doe@example.com")
        val password = Password("password123")
        val user = DefinedUser(ValidUserId("123"), Name("John Doe"), email, password)
        val loginRequest = LoginRequest(email, password)
        
        every { userRepository.findByEmail(email) } returns user
        
        val loginUseCase = LoginUseCase(userRepository)
        
        // Act
        loginUseCase.unfold(loginRequest)
        
        // Assert
        val authenticatedUser = loginUseCase.authenticatedUser
        assertEquals(user, authenticatedUser)
        verify { userRepository.findByEmail(email) }
    }

    @Test
    fun `should fail when user does not exist`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val email = Email("nonexistent@example.com")
        val password = Password("password123")
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
        assertEquals("User with email nonexistent@example.com does not exist.", exception.message)
    }

    @Test
    fun `should fail when password is incorrect`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val email = Email("john.doe@example.com")
        val correctPassword = Password("password123")
        val incorrectPassword = Password("wrongpassword")
        val user = DefinedUser(ValidUserId("123"), Name("John Doe"), email, correctPassword)
        val loginRequest = LoginRequest(email, incorrectPassword)
        
        every { userRepository.findByEmail(email) } returns user
        
        val loginUseCase = LoginUseCase(userRepository)
        
        // Act & Assert
        val exception = assertThrows<InvalidCredentialsException> {
            loginUseCase.unfold(loginRequest)
        }
        assertEquals("Invalid credentials.", exception.message)
    }
}
