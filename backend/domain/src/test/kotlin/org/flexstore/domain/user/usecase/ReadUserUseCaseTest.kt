package org.flexstore.domain.user.usecase

import org.flexstore.domain.shared.*
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.model.UserId.InvalidUserId
import org.flexstore.domain.user.model.UserId.ValidUserId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class ReadUserUseCaseTest {

    @Test
    fun `should read user successfully`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val userId = ValidUserId("123")
        val user = User.DefinedUser(userId, Name("John Doe"), Email("john.doe@example.com"), PlainPassword("password123"))
        every { userRepository.findById(userId) } returns user
        every { userRepository.notExists(userId) } returns false
        val readUserUseCase = ReadUserUseCase(userRepository)
        // Act
        readUserUseCase.unfold(userId)
        // Assert
        val retrievedUser = readUserUseCase.retrievedUser
        assertEquals(user, retrievedUser)
        verify { userRepository.findById(userId) }
    }

    @Test
    fun `should fail if user id is invalid`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val invalidUerId = InvalidUserId("@")
        val readUserUseCase = ReadUserUseCase(userRepository)
        val exception = assertThrows<InvalidUserIdException> {
            // Act
            readUserUseCase.unfold(invalidUerId)
        }
        // Assert
        assertEquals("User ID @ is invalid.", exception.message)
        verify(exactly = 0) { userRepository.exists(invalidUerId) }
    }

    @Test
    fun `should fail if user does not exist`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val userId = ValidUserId("123")
        every { userRepository.notExists(userId) } returns true
        val readUserUseCase = ReadUserUseCase(userRepository)
        val exception = assertThrows<UserNotFoundException> {
            // Act
            readUserUseCase.unfold(userId)
        }
        // Assert
        assertEquals("User with ID 123 does not exist.", exception.message)
        verify { userRepository.notExists(userId) }
    }
}