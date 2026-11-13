package org.flexstore.domain.user.administration.usecase

import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.model.UserId.ValidUserId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.flexstore.domain.user.repository.*
import org.junit.jupiter.api.Test

class DeleteUserUseCaseTest {

    private val administrator = ValidUserId("admin-1")
    private val userRepository = mockk<UserRepository> {
        every { isNotAdministrator(administrator) } returns false
    }
    private val deleteUserUseCase = DeleteUserUseCase(userRepository)
    private val userId = ValidUserId("123")

    @Test
    fun `should delete user successfully`() {
        // Arrange
        every { userRepository.notExists(userId) } returns false
        every { userRepository.exists(userId) } returns false
        every { userRepository.delete(userId) } returns true
        // Act
        deleteUserUseCase.unfold(UserDeletion(administrator, userId))
        // Assert
        verify { userRepository.delete(userId) }
    }

    @Test
    fun `should throw UserNotFoundException when user does not exist`() {
        // Arrange
        every { userRepository.notExists(userId) } returns true
        // Act
        val thrown = catchThrowable { deleteUserUseCase.unfold(UserDeletion(administrator, userId)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `should throw UserDeletionFailed when user deletion fails`() {
        // Arrange
        every { userRepository.exists(userId) } returns true
        every { userRepository.delete(userId) } returns false
        every { userRepository.notExists(userId) } returns false
        // Act
        val thrown = catchThrowable { deleteUserUseCase.unfold(UserDeletion(administrator, userId)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserDeletionFailed::class.java)
    }

    @Test
    fun `a customer should not delete a user`() {
        // Arrange
        val customer = ValidUserId("customer-1")
        every { userRepository.isNotAdministrator(customer) } returns true
        // Act
        val thrown = catchThrowable { deleteUserUseCase.unfold(UserDeletion(customer, userId)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserDeletionForbiddenException::class.java)
        verify(exactly = 0) { userRepository.delete(userId) }
    }
}
