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

class CreateUserUseCaseTest {

    private val userRepository = mockk<UserRepository>()
    private val createUserUseCase = CreateUserUseCase(userRepository)
    private val user = DefinedUser(ValidUserId("123"), Name("John Doe"), Email("john.doe@example.com"), PlainPassword("password123"))
    private val draftUser = User.DraftUser(Name("John Doe"), Email("john.doe@example.com"), PlainPassword("password123"))

    @Test
    fun `should create user successfully`() {
        // Arrange
        every { userRepository.save(ofType<User.DefinedUser>()) } answers { firstArg() }
        every { userRepository.exists(user.id) } returns false
        every { userRepository.notExists(user.id) } returns false
        // Act
        createUserUseCase.unfold(user)
        // Assert
        verify(exactly = 1) { userRepository.save(ofType<User.DefinedUser>()) }
    }

    @Test
    fun `should fail if user already exists`() {
        // Arrange
        every { userRepository.exists(user.id) } returns true
        // Act
        val thrown = catchThrowable { createUserUseCase.unfold(user) }
        // Assert
        assertThat(thrown).isInstanceOf(UserAlreadyExists::class.java).hasMessage("User with ID 123 already exists.")
    }

    @Test
    fun `should fail if user is not created`() {
        // Arrange
        every { userRepository.notExists(user.id) } returns true
        every { userRepository.save(ofType<User.DefinedUser>()) } returns user
        every { userRepository.exists(user.id) } returns false
        // Act
        val thrown = catchThrowable { createUserUseCase.unfold(user) }
        // Assert
        assertThat(thrown).isInstanceOf(UserCreationFailed::class.java).hasMessage("User with ID 123 was not created.")
    }

    @Test
    fun `should create draft user with the id assigned by the repository`() {
        // Arrange
        val savedUser = DefinedUser(ValidUserId("generated-id"), draftUser.name, draftUser.email, PlainPassword("password123"))
        every { userRepository.emailIsAlreadyUsed(draftUser.email) } returns false
        every { userRepository.save(draftUser) } returns savedUser
        every { userRepository.notExists(savedUser.id) } returns false
        // Act
        createUserUseCase.unfold(draftUser)
        // Assert
        assertThat(createUserUseCase.createdUser).isEqualTo(savedUser)
    }

    @Test
    fun `should fail if draft user email is already used`() {
        // Arrange
        every { userRepository.emailIsAlreadyUsed(draftUser.email) } returns true
        // Act
        val thrown = catchThrowable { createUserUseCase.unfold(draftUser) }
        // Assert
        assertThat(thrown).isInstanceOf(UserAlreadyExists::class.java)
            .hasMessage("User with email john.doe@example.com already exists.")
    }

    @Test
    fun `should fail if draft user password is blank`() {
        // Arrange
        val draftUserWithBlankPassword = draftUser.copy(password = PlainPassword(" "))
        every { userRepository.emailIsAlreadyUsed(draftUserWithBlankPassword.email) } returns false
        // Act
        val thrown = catchThrowable { createUserUseCase.unfold(draftUserWithBlankPassword) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidPasswordException::class.java).hasMessage("Password must not be blank.")
    }
}
