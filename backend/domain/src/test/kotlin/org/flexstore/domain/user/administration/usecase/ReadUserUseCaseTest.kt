package org.flexstore.domain.user.administration.usecase

import org.flexstore.domain.actor.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.model.UserId.InvalidUserId
import org.flexstore.domain.user.model.UserId.ValidUserId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import kotlin.test.Test
import org.flexstore.domain.user.repository.*

class ReadUserUseCaseTest {

    private val userRepository = mockk<UserRepository>()
    private val readUserUseCase = ReadUserUseCase(userRepository)
    private val userId = ValidUserId("123")

    @Test
    fun `should read user successfully`() {
        // Arrange
        val user = User.DefinedUser(
            userId,
            FullName(FirstName("John"), LastName("Doe")),
            Email("john.doe@example.com"),
            PlainPassword("password123")
        )
        every { userRepository.findById(userId) } returns user
        every { userRepository.notExists(userId) } returns false
        // Act
        readUserUseCase.unfold(UserReading(testAdministratorActor, userId))
        // Assert
        assertThat(readUserUseCase.retrievedUser).isEqualTo(user)
    }

    @Test
    fun `should fail if user id is invalid`() {
        // Arrange
        val invalidUserId = InvalidUserId("@")
        // Act
        val reading = UserReading(testAdministratorActor, invalidUserId)
        val thrown = catchThrowable { readUserUseCase.unfold(reading) }
        // Assert
        assertThat(thrown)
            .hasCauseInstanceOf(InvalidUserIdException::class.java)
            .hasMessage("User ID @ is invalid.")
        verify(exactly = 0) { userRepository.exists(invalidUserId) }
    }

    @Test
    fun `should fail if user does not exist`() {
        // Arrange
        every { userRepository.notExists(userId) } returns true
        // Act
        val thrown = catchThrowable { readUserUseCase.unfold(UserReading(testAdministratorActor, userId)) }
        // Assert
        assertThat(thrown)
            .hasCauseInstanceOf(UserNotFoundException::class.java)
            .hasMessage("User with ID 123 does not exist.")
    }

    @Test
    fun `a customer should not read a user`() {
        // Arrange
        // Act
        val thrown = catchThrowable { readUserUseCase.unfold(UserReading(testCustomerActor, userId)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserReadingForbiddenException::class.java)
        verify(exactly = 0) { userRepository.findById(userId) }
    }
}
