package org.flexstore.domain.usecase.user

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.flexstore.domain.entity.*
import org.flexstore.domain.entity.User.DefinedUser
import org.flexstore.domain.entity.UserId.ValidUserId
import org.flexstore.domain.port.PasswordEncoder
import org.flexstore.domain.repository.UserRepository
import org.flexstore.domain.valueobject.Name
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class CreateUserUseCaseTest {

    @Test
    fun `should create user successfully`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val passwordEncoder = mockk<PasswordEncoder>()
        val user = DefinedUser(ValidUserId("123"), Name("John Doe"), Email("john.doe@example.com"), Password("password123"))
        val hashedPassword = "hashedPassword123"
        
        every { passwordEncoder.encode("password123") } returns hashedPassword
        every { userRepository.save(ofType<User.DefinedUser>()) } answers { firstArg() }
        every { userRepository.exists(user.id) } returns false
        every { userRepository.notExists(user.id) } returns false
        
        val createUserUseCase = CreateUserUseCase(userRepository, passwordEncoder)
        
        // Act
        createUserUseCase.unfold(user)
        
        // Assert
        verify { userRepository.notExists(user.id) }
        verify { passwordEncoder.encode("password123") }
        verify(exactly = 1) { userRepository.save(ofType<User.DefinedUser>()) }
        verify { userRepository.exists(user.id) }
    }

    @Test
    fun `should fail if user already exists`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val passwordEncoder = mockk<PasswordEncoder>()
        val user = DefinedUser(ValidUserId("123"), Name("John Doe"), Email("john.doe@example.com"), Password("password123"))
        every { userRepository.exists(user.id) } returns true
        val createUserUseCase = CreateUserUseCase(userRepository, passwordEncoder)
        val exception = assertThrows<UserAlreadyExists> {
            // Act
            createUserUseCase.unfold(user)
        }
        // Assert
        assertEquals("User with ID 123 already exists.", exception.message)
    }

    @Test
    fun `should fail if user is not created`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val passwordEncoder = mockk<PasswordEncoder>()
        val user = DefinedUser(ValidUserId("123"), Name("John Doe"), Email("john.doe@example.com"), Password("password123"))
        val hashedPassword = "hashedPassword123"
        
        every { passwordEncoder.encode("password123") } returns hashedPassword
        every { userRepository.notExists(user.id) } returns true
        every { userRepository.save(ofType<User.DefinedUser>()) } returns user
        every { userRepository.exists(user.id) } returns false
        
        // Act
        val createUserUseCase = CreateUserUseCase(userRepository, passwordEncoder)
        val exception = assertThrows<UserCreationFailed> {
            createUserUseCase.unfold(user)
        }
        // Assert
        assertEquals("User with ID 123 was not created.", exception.message)
    }
}

