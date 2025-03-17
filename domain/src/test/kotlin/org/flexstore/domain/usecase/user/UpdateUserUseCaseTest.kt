package org.flexstore.domain.usecase.user

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.flexstore.domain.entity.*
import org.flexstore.domain.entity.User.DefinedUser
import org.flexstore.domain.entity.UserId.InvalidUserId
import org.flexstore.domain.entity.UserId.ValidUserId
import org.flexstore.domain.repository.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.ucop.domain.entity.Name
import kotlin.test.Test

class UpdateUserUseCaseTest {

    @Test
    fun `should update user successfully`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val userId = ValidUserId("123")
        val user = DefinedUser(userId, Name("John Doe"), Email("john.doe@example.com"))
        every { userRepository.exists(userId) } returns true
        every { userRepository.save(user) } returns Unit
        val updateUserUseCase = UpdateUserUseCase(userRepository)
        // Act
        updateUserUseCase.run(user)
        // Assert
        verify { userRepository.save(user) }
    }

    @Test
    fun `should fail to update if user id is invalid`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val invalidUserId = InvalidUserId("@")
        val user = DefinedUser(invalidUserId, Name("John Doe"), Email("john.doe@example.com"))
        val updateUserUseCase = UpdateUserUseCase(userRepository)
        val exception = assertThrows<InvalidUserIdException> {
            // Act
            updateUserUseCase.run(user)
        }
        // Assert
        assertEquals("User ID @ is invalid.", exception.message)
        verify(exactly = 0) { userRepository.save(user) }
    }

    @Test
    fun `should fail to update if user does not exist`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val userId = ValidUserId("123")
        val user = DefinedUser(userId, Name("John Doe"), Email("john.doe@example.com"))
        every { userRepository.exists(userId) } returns false
        val updateUserUseCase = UpdateUserUseCase(userRepository)
        val exception = assertThrows<UserNotFoundException> {
            // Act
            updateUserUseCase.run(user)
        }
        // Assert
        assertEquals("User with ID 123 does not exist.", exception.message)
        verify(exactly = 0) { userRepository.save(user) }
    }
}