package org.flexstore.domain.usecase

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.flexstore.domain.entity.Email
import org.flexstore.domain.entity.User.DefinedUser
import org.flexstore.domain.entity.UserDeletionFailed
import org.flexstore.domain.entity.UserId.ValidUserId
import org.flexstore.domain.entity.UserNotFoundException
import org.flexstore.domain.repository.UserRepository
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.ucop.domain.entity.Name

class DeleteUserUseCaseTest {

    @Test
    fun `should delete user successfully`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val user = DefinedUser(ValidUserId("123"), Name("Jane Doe"), Email("jane.doe@example.com"))
        every { userRepository.exists(user.id) } returns true
        every { userRepository.delete(user.id) } returns true
        every { userRepository.notExists(user.id) } returns true

        val deleteUserUseCase = DeleteUserUseCase(userRepository)

        // Act
        deleteUserUseCase.run(user)

        // Assert
        verify { userRepository.delete(user.id) }
    }

    @Test
    fun `should throw UserNotFoundException when user does not exist`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val user = DefinedUser(ValidUserId("123"), Name("Jane Doe"), Email("jane.doe@example.com"))
        every { userRepository.exists(user.id) } returns false

        val deleteUserUseCase = DeleteUserUseCase(userRepository)

        // Act & Assert
        assertThrows(UserNotFoundException::class.java) {
            deleteUserUseCase.run(user)
        }
    }

    @Test
    fun `should throw UserDeletionFailed when user deletion fails`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val user = DefinedUser(ValidUserId("123"), Name("Jane Doe"), Email("jane.doe@example.com"))
        every { userRepository.exists(user.id) } returns true
        every { userRepository.delete(user.id) } returns false

        val deleteUserUseCase = DeleteUserUseCase(userRepository)

        // Act & Assert
        assertThrows(UserDeletionFailed::class.java) {
            deleteUserUseCase.run(user)
        }
    }
}