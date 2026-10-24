package org.flexstore.domain.user.usecase

import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.model.User.DefinedUser
import org.flexstore.domain.user.model.UserId.ValidUserId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test

class ChangePasswordUseCaseTest {

    private val userId = ValidUserId("123")
    private val email = Email("john.doe@example.com")
    private val user = DefinedUser(userId, Name("John Doe"), email, HashedPassword("hashed"))
    private val currentPassword = PlainPassword("current-password")
    private val newPassword = PlainPassword("new-password")

    private fun repository(currentPasswordMatches: Boolean = true): UserRepository {
        val userRepository = mockk<UserRepository>()
        every { userRepository.notExists(userId) } returns false
        every { userRepository.findById(userId) } returns user
        every { userRepository.passwordDoesNotMatch(email, currentPassword) } returns !currentPasswordMatches
        every { userRepository.save(ofType<DefinedUser>()) } answers { firstArg() }
        every { userRepository.passwordDoesNotMatch(email, newPassword) } returns false
        return userRepository
    }

    @Test
    fun `should save the user with the new password`() {
        // Arrange
        val userRepository = repository()
        // Act
        ChangePasswordUseCase(userRepository).unfold(ChangePasswordRequest(userId, currentPassword, newPassword))
        // Assert
        verify { userRepository.save(user.copy(password = newPassword)) }
    }

    @Test
    fun `should fail if the current password is wrong`() {
        // Arrange
        val userRepository = repository(currentPasswordMatches = false)
        // Act
        val exception = assertThrows<InvalidCurrentPasswordException> {
            ChangePasswordUseCase(userRepository).unfold(ChangePasswordRequest(userId, currentPassword, newPassword))
        }
        // Assert
        assertEquals("The current password is wrong.", exception.message)
        verify(exactly = 0) { userRepository.save(ofType<DefinedUser>()) }
    }

    @Test
    fun `should fail if the new password is too short`() {
        // Arrange
        val userRepository = repository()
        // Act
        val exception = assertThrows<InvalidPasswordException> {
            ChangePasswordUseCase(userRepository).unfold(ChangePasswordRequest(userId, currentPassword, PlainPassword("short")))
        }
        // Assert
        assertEquals("Password must be at least 8 characters.", exception.message)
        verify(exactly = 0) { userRepository.save(ofType<DefinedUser>()) }
    }

    @Test
    fun `should fail if the user does not exist`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        every { userRepository.notExists(userId) } returns true
        // Act
        val exception = assertThrows<UserNotFoundException> {
            ChangePasswordUseCase(userRepository).unfold(ChangePasswordRequest(userId, currentPassword, newPassword))
        }
        // Assert
        assertEquals("User with ID 123 does not exist.", exception.message)
    }
}
