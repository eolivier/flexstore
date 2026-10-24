package org.flexstore.domain.user.usecase

import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.*
import org.flexstore.domain.user.UserId.InvalidUserId
import org.flexstore.domain.user.UserId.ValidUserId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import kotlin.test.Test

class ReadUserUseCaseTest {

    private val userRepository = mockk<UserRepository>()
    private val readUserUseCase = ReadUserUseCase(userRepository)
    private val userId = ValidUserId("123")

    @Test
    fun `should read user successfully`() {
        // Arrange
        val user = User.DefinedUser(userId, Name("John Doe"), Email("john.doe@example.com"), PlainPassword("password123"))
        every { userRepository.findById(userId) } returns user
        every { userRepository.notExists(userId) } returns false
        // Act
        readUserUseCase.unfold(userId)
        // Assert
        assertThat(readUserUseCase.retrievedUser).isEqualTo(user)
    }

    @Test
    fun `should fail if user id is invalid`() {
        // Arrange
        val invalidUserId = InvalidUserId("@")
        // Act
        val thrown = catchThrowable { readUserUseCase.unfold(invalidUserId) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidUserIdException::class.java).hasMessage("User ID @ is invalid.")
        verify(exactly = 0) { userRepository.exists(invalidUserId) }
    }

    @Test
    fun `should fail if user does not exist`() {
        // Arrange
        every { userRepository.notExists(userId) } returns true
        // Act
        val thrown = catchThrowable { readUserUseCase.unfold(userId) }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException::class.java).hasMessage("User with ID 123 does not exist.")
    }
}
