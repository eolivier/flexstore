package org.flexstore.domain.usecase.user

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.flexstore.domain.entity.Email
import org.flexstore.domain.entity.User.DefinedUser
import org.flexstore.domain.entity.UserAlreadyExists
import org.flexstore.domain.entity.UserCreationFailed
import org.flexstore.domain.entity.UserId.ValidUserId
import org.flexstore.domain.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import org.ucop.domain.entity.Name
import kotlin.test.Test

class CreateUserUseCaseTest {

    @Test
    fun `should create user successfully`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val user = DefinedUser(ValidUserId("123"), Name("John Doe"), Email("john.doe@example.com"))
        every { userRepository.notExists(user.id) } returns true
        every { userRepository.save(user) } returns Unit
        every { userRepository.exists(user.id) } returns true
        val createUserUseCase = CreateUserUseCase(userRepository)
        // Act
        createUserUseCase.unfold(user)
        // Assert
        verify { userRepository.notExists(user.id) }
        verify { userRepository.save(user) }
        verify { userRepository.exists(user.id) }
    }

    @Test
    fun `should fail if user already exists`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val user = DefinedUser(ValidUserId("123"), Name("John Doe"), Email("john.doe@example.com"))
        every { userRepository.notExists(user.id) } returns false
        val createUserUseCase = CreateUserUseCase(userRepository)
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
        val user = DefinedUser(ValidUserId("123"), Name("John Doe"), Email("john.doe@example.com"))
        every { userRepository.notExists(user.id) } returns true
        every { userRepository.save(user) } returns Unit
        every { userRepository.exists(user.id) } returns false
        // Act
        val createUserUseCase = CreateUserUseCase(userRepository)
        val exception = assertThrows<UserCreationFailed> {
            createUserUseCase.unfold(user)
        }
        // Assert
        assertEquals("User with ID 123 was not created.", exception.message)
    }
}

