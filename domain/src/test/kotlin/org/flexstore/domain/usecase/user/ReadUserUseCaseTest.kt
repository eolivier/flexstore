package org.flexstore.domain.usecase.user

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.flexstore.domain.entity.Email
import org.flexstore.domain.entity.User
import org.flexstore.domain.entity.User.UndefinedUser
import org.flexstore.domain.entity.UserId.InvalidUserId
import org.flexstore.domain.entity.UserId.ValidUserId
import org.flexstore.domain.entity.UserNotFoundException
import org.flexstore.domain.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.ucop.domain.entity.Name
import kotlin.test.Test

class ReadUserUseCaseTest {

    @Test
    fun `should read user successfully`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val userId = ValidUserId("123")
        val user = User.DefinedUser(userId, Name("John Doe"), Email("john.doe@example.com"))
        every { userRepository.findById(userId) } returns user
        every { userRepository.exists(userId) } returns true
        val readUserUseCase = ReadUserUseCase(userRepository)
        // Act
        readUserUseCase.run(userId)
        // Assert
        val retrievedUser = readUserUseCase.retrievedUser
        assertEquals(user, retrievedUser)
        verify { userRepository.findById(userId) }
    }

    @Test
    fun `should fail if user does not exist`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val userId = ValidUserId("123")
        val invalidUerId = InvalidUserId("")
        every { userRepository.exists(userId) } returns false
        every { userRepository.findById(userId) } returns UndefinedUser(invalidUerId, "Invalid user id")
        val readUserUseCase = ReadUserUseCase(userRepository)
        val exception = assertThrows<UserNotFoundException> {
            // Act
            readUserUseCase.run(userId)
        }
        // Assert
        assertEquals("User with ID 123 does not exist.", exception.message)
        verify { userRepository.findById(userId) }
    }
}