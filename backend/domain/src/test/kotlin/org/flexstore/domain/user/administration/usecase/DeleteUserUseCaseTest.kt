package org.flexstore.domain.user.administration.usecase

import org.flexstore.domain.actor.*
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
import org.flexstore.domain.user.customerOf
import org.flexstore.domain.user.systemUser

class DeleteUserUseCaseTest {

    private val userRepository = mockk<UserRepository> {
        every { findById(ValidUserId("123")) } returns customerOf("123")
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
        deleteUserUseCase.unfold(UserDeletion(testAdministratorActor, userId))
        // Assert
        verify { userRepository.delete(userId) }
    }

    @Test
    fun `should refuse to delete the system user`() {
        // Arrange
        every { userRepository.notExists(SystemUser.id) } returns false
        every { userRepository.findById(SystemUser.id) } returns systemUser
        // Act
        val thrown = catchThrowable { deleteUserUseCase.unfold(UserDeletion(testAdministratorActor, SystemUser.id)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(SystemRoleForbiddenException::class.java)
            .hasMessage("The system user cannot be deleted.")
    }

    @Test
    fun `should throw UserNotFoundException when user does not exist`() {
        // Arrange
        every { userRepository.notExists(userId) } returns true
        // Act
        val thrown = catchThrowable { deleteUserUseCase.unfold(UserDeletion(testAdministratorActor, userId)) }
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
        val thrown = catchThrowable { deleteUserUseCase.unfold(UserDeletion(testAdministratorActor, userId)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserDeletionFailed::class.java)
    }

    @Test
    fun `a customer should not delete a user`() {
        // Arrange
        // Act
        val thrown = catchThrowable { deleteUserUseCase.unfold(UserDeletion(testCustomerActor, userId)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserDeletionForbiddenException::class.java)
        verify(exactly = 0) { userRepository.delete(userId) }
    }
}
