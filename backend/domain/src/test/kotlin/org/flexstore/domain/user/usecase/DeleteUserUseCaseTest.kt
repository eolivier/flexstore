package org.flexstore.domain.user.usecase

import org.flexstore.domain.user.*
import org.flexstore.domain.user.UserId.ValidUserId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test

class DeleteUserUseCaseTest {

    private val userRepository = mockk<UserRepository>()
    private val deleteUserUseCase = DeleteUserUseCase(userRepository)
    private val userId = ValidUserId("123")

    @Test
    fun `should delete user successfully`() {
        // Arrange
        every { userRepository.notExists(userId) } returns false
        every { userRepository.exists(userId) } returns false
        every { userRepository.delete(userId) } returns true
        // Act
        deleteUserUseCase.unfold(userId)
        // Assert
        verify { userRepository.delete(userId) }
    }

    @Test
    fun `should throw UserNotFoundException when user does not exist`() {
        // Arrange
        every { userRepository.notExists(userId) } returns true
        // Act
        val thrown = catchThrowable { deleteUserUseCase.unfold(userId) }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `should throw UserDeletionFailed when user deletion fails`() {
        // Arrange
        every { userRepository.exists(userId) } returns true
        every { userRepository.delete(userId) } returns false
        every { userRepository.notExists(userId) } returns false
        // Act
        val thrown = catchThrowable { deleteUserUseCase.unfold(userId) }
        // Assert
        assertThat(thrown).isInstanceOf(UserDeletionFailed::class.java)
    }
}
