package org.flexstore.domain.user.usecase

import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.*
import org.flexstore.domain.user.User.DefinedUser
import org.flexstore.domain.user.UserId.ValidUserId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import kotlin.test.Test

class ChangePasswordUseCaseTest {

    private val userId = ValidUserId("123")
    private val email = Email("john.doe@example.com")
    private val user = DefinedUser(userId, FullName(FirstName("John"), LastName("Doe")), email, HashedPassword("hashed"))
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
        val thrown = catchThrowable { ChangePasswordUseCase(userRepository).unfold(ChangePasswordRequest(userId, currentPassword, newPassword)) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidCurrentPasswordException::class.java).hasMessage("The current password is wrong.")
        verify(exactly = 0) { userRepository.save(ofType<DefinedUser>()) }
    }

    @Test
    fun `should fail if the new password is too short`() {
        // Arrange
        val userRepository = repository()
        // Act
        val thrown = catchThrowable { ChangePasswordUseCase(userRepository).unfold(ChangePasswordRequest(userId, currentPassword, PlainPassword("short"))) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidPasswordException::class.java).hasMessage("Password must be at least 8 characters.")
        verify(exactly = 0) { userRepository.save(ofType<DefinedUser>()) }
    }

    @Test
    fun `should fail if the user does not exist`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        every { userRepository.notExists(userId) } returns true
        // Act
        val thrown = catchThrowable { ChangePasswordUseCase(userRepository).unfold(ChangePasswordRequest(userId, currentPassword, newPassword)) }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException::class.java).hasMessage("User with ID 123 does not exist.")
    }
}
