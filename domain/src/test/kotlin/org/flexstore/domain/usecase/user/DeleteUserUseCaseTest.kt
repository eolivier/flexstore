package org.flexstore.domain.usecase.user

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.flexstore.domain.entity.UserDeletionFailed
import org.flexstore.domain.entity.UserId.ValidUserId
import org.flexstore.domain.entity.UserNotFoundException
import org.flexstore.domain.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class DeleteUserUseCaseTest {

    @Test
    fun `should delete user successfully`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val validUserId = ValidUserId("123")
        every { userRepository.notExists(validUserId) } returns false
        every { userRepository.exists(validUserId) } returns false
        every { userRepository.delete(validUserId) } returns true
        val deleteUserUseCase = DeleteUserUseCase(userRepository)
        // Act
        deleteUserUseCase.unfold(validUserId)
        // Assert
        verify { userRepository.delete(validUserId) }
    }

    @Test
    fun `should throw UserNotFoundException when user does not exist`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val validUserId = ValidUserId("123")
        every { userRepository.notExists(validUserId) } returns true
        val deleteUserUseCase = DeleteUserUseCase(userRepository)
        // Act & Assert
        deleteUserUseCase.getAlternativeScenarii()
        assertThrows(UserNotFoundException::class.java) {
            deleteUserUseCase.unfold(validUserId)
        }
    }

    @Test
    fun `should throw UserDeletionFailed when user deletion fails`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val validUserId = ValidUserId("123")
        every { userRepository.exists(validUserId) } returns true
        every { userRepository.delete(validUserId) } returns false
        every { userRepository.notExists(validUserId) } returns false
        val deleteUserUseCase = DeleteUserUseCase(userRepository)
        // Act & Assert
        assertThrows(UserDeletionFailed::class.java) {
            deleteUserUseCase.unfold(validUserId)
        }
    }
}