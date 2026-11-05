package org.flexstore.domain.user.administration.usecase

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

    private val administrator = ValidUserId("admin-1")
    private val userRepository = mockk<UserRepository> {
        every { isNotAdministrator(administrator) } returns false
    }
    private val readUserUseCase = ReadUserUseCase(userRepository)
    private val userId = ValidUserId("123")

    @Test
    fun `should read user successfully`() {
        // Arrange
        val user = User.DefinedUser(userId, FullName(FirstName("John"), LastName("Doe")), Email("john.doe@example.com"), PlainPassword("password123"))
        every { userRepository.findById(userId) } returns user
        every { userRepository.notExists(userId) } returns false
        // Act
        readUserUseCase.unfold(UserReading(administrator, userId))
        // Assert
        assertThat(readUserUseCase.retrievedUser).isEqualTo(user)
    }

    @Test
    fun `should fail if user id is invalid`() {
        // Arrange
        val invalidUserId = InvalidUserId("@")
        // Act
        val thrown = catchThrowable { readUserUseCase.unfold(UserReading(administrator, invalidUserId)) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidUserIdException::class.java).hasMessage("User ID @ is invalid.")
        verify(exactly = 0) { userRepository.exists(invalidUserId) }
    }

    @Test
    fun `should fail if user does not exist`() {
        // Arrange
        every { userRepository.notExists(userId) } returns true
        // Act
        val thrown = catchThrowable { readUserUseCase.unfold(UserReading(administrator, userId)) }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException::class.java).hasMessage("User with ID 123 does not exist.")
    }

    @Test
    fun `a customer should not read a user`() {
        // Arrange
        val customer = ValidUserId("customer-1")
        every { userRepository.isNotAdministrator(customer) } returns true
        // Act
        val thrown = catchThrowable { readUserUseCase.unfold(UserReading(customer, userId)) }
        // Assert
        assertThat(thrown).isInstanceOf(UserReadingForbiddenException::class.java)
        verify(exactly = 0) { userRepository.findById(userId) }
    }
}
