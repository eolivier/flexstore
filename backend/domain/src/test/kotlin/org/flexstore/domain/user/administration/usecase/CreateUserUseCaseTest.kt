package org.flexstore.domain.user.administration.usecase

import org.flexstore.domain.actor.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.model.User.DefinedUser
import org.flexstore.domain.user.model.UserId.ValidUserId
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import kotlin.test.Test
import org.flexstore.domain.user.repository.*

class CreateUserUseCaseTest {

    private val userRepository = mockk<UserRepository>()
    private val createUserUseCase = CreateUserUseCase(userRepository)
    private val user = DefinedUser(ValidUserId("123"), FullName(FirstName("John"), LastName("Doe")), Email("john.doe@example.com"), PlainPassword("password123"))
    private val draftUser = User.DraftUser(FullName(FirstName("John"), LastName("Doe")), Email("john.doe@example.com"), PlainPassword("password123"))

    @Test
    fun `should create user successfully`() {
        // Arrange
        every { userRepository.save(ofType<User.DefinedUser>()) } answers { firstArg() }
        every { userRepository.exists(user.id) } returns false
        every { userRepository.notExists(user.id) } returns false
        // Act
        createUserUseCase.unfold(CreateUserRequest(testAdministratorActor, user))
        // Assert
        verify(exactly = 1) { userRepository.save(user) }
    }

    @Test
    fun `should refuse to give the SYSTEM role to a new user`() {
        // Arrange
        existing(administrator)
        val systemDraftUser = draftUser.copy(role = Role.SYSTEM)
        // Act
        val request = CreateUserRequest(administrator.id, systemDraftUser)
        val thrown = catchThrowable { createUserUseCase.unfold(request) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(SystemRoleForbiddenException::class.java)
    }

    @Test
    fun `should refuse a creator who is not an administrator`() {
        // Arrange
        // Act
        val thrown = catchThrowable { createUserUseCase.unfold(CreateUserRequest(testCustomerActor, draftUser)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserCreationForbiddenException::class.java)
        verify(exactly = 0) { userRepository.save(ofType<User.DraftUser>()) }
    }

    @Test
    fun `should refuse an unknown creator`() {
        // Arrange
        // Act
        val thrown = catchThrowable { createUserUseCase.unfold(CreateUserRequest(unknownUserActor, draftUser)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserCreationForbiddenException::class.java)
    }

    @Test
    fun `should fail if user already exists`() {
        // Arrange
        every { userRepository.exists(user.id) } returns true
        // Act
        val thrown = catchThrowable { createUserUseCase.unfold(CreateUserRequest(testAdministratorActor, user)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserAlreadyExists::class.java).hasMessage("User with ID 123 already exists.")
    }

    @Test
    fun `should fail if user is not created`() {
        // Arrange
        every { userRepository.notExists(user.id) } returns true
        every { userRepository.save(ofType<User.DefinedUser>()) } returns user
        every { userRepository.exists(user.id) } returns false
        // Act
        val thrown = catchThrowable { createUserUseCase.unfold(CreateUserRequest(testAdministratorActor, user)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserCreationFailed::class.java)
            .hasMessage("User with ID 123 was not created.")
    }

    @Test
    fun `should create draft user with the id assigned by the repository`() {
        // Arrange
        val savedUser = DefinedUser(ValidUserId("generated-id"), draftUser.fullName, draftUser.email, PlainPassword("password123"))
        every { userRepository.emailIsAlreadyUsed(draftUser.email) } returns false
        every { userRepository.save(draftUser) } returns savedUser
        every { userRepository.notExists(savedUser.id) } returns false
        // Act
        createUserUseCase.unfold(CreateUserRequest(testAdministratorActor, draftUser))
        // Assert
        assertThat(createUserUseCase.createdUser).isEqualTo(savedUser)
    }

    @Test
    fun `should fail if draft user email is already used`() {
        // Arrange
        every { userRepository.emailIsAlreadyUsed(draftUser.email) } returns true
        // Act
        val thrown = catchThrowable { createUserUseCase.unfold(CreateUserRequest(testAdministratorActor, draftUser)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserAlreadyExists::class.java)
            .hasMessage("User with email john.doe@example.com already exists.")
    }

    @Test
    fun `should fail if draft user password is blank`() {
        // Arrange
        val draftUserWithBlankPassword = draftUser.copy(password = PlainPassword(" "))
        every { userRepository.emailIsAlreadyUsed(draftUserWithBlankPassword.email) } returns false
        val createUserRequest = CreateUserRequest(testAdministratorActor, draftUserWithBlankPassword)
        // Act
        val thrown = catchThrowable { createUserUseCase.unfold(createUserRequest) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(InvalidPasswordException::class.java).hasMessage("Password must not be blank.")
    }
}
