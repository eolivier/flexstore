package org.flexstore.domain.user.usecase

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

class CreateUserUseCaseTest {

    private val userRepository = mockk<UserRepository>()
    private val createUserUseCase = CreateUserUseCase(userRepository)
    private val administrator = DefinedUser(ValidUserId("admin-1"), FullName(FirstName("Admin"), LastName("Flexstore")), Email("admin@example.com"), HashedPassword("hashed"), Role.ADMIN)
    private val user = DefinedUser(ValidUserId("123"), FullName(FirstName("John"), LastName("Doe")), Email("john.doe@example.com"), PlainPassword("password123"))
    private val draftUser = User.DraftUser(FullName(FirstName("John"), LastName("Doe")), Email("john.doe@example.com"), PlainPassword("password123"))

    private fun existing(creator: DefinedUser) {
        every { userRepository.isNotAdministrator(creator.id) } returns creator.isNotAdministrator()
    }

    @Test
    fun `should create user successfully`() {
        // Arrange
        existing(administrator)
        every { userRepository.save(ofType<User.DefinedUser>()) } answers { firstArg() }
        every { userRepository.exists(user.id) } returns false
        every { userRepository.notExists(user.id) } returns false
        // Act
        createUserUseCase.unfold(CreateUserRequest(administrator.id, user))
        // Assert
        verify(exactly = 1) { userRepository.save(user) }
    }

    @Test
    fun `should refuse a creator who is not an administrator`() {
        // Arrange
        val creator = administrator.copy(role = Role.USER)
        existing(creator)
        // Act
        val thrown = catchThrowable { createUserUseCase.unfold(CreateUserRequest(creator.id, draftUser)) }
        // Assert
        assertThat(thrown).isInstanceOf(UserCreationForbiddenException::class.java)
        verify(exactly = 0) { userRepository.save(ofType<User.DraftUser>()) }
    }

    @Test
    fun `should refuse an unknown creator`() {
        // Arrange
        val unknownCreator = ValidUserId("unknown")
        every { userRepository.isNotAdministrator(unknownCreator) } returns true
        // Act
        val thrown = catchThrowable { createUserUseCase.unfold(CreateUserRequest(unknownCreator, draftUser)) }
        // Assert
        assertThat(thrown).isInstanceOf(UserCreationForbiddenException::class.java)
    }

    @Test
    fun `should fail if user already exists`() {
        // Arrange
        existing(administrator)
        every { userRepository.exists(user.id) } returns true
        // Act
        val thrown = catchThrowable { createUserUseCase.unfold(CreateUserRequest(administrator.id, user)) }
        // Assert
        assertThat(thrown).isInstanceOf(UserAlreadyExists::class.java).hasMessage("User with ID 123 already exists.")
    }

    @Test
    fun `should fail if user is not created`() {
        // Arrange
        existing(administrator)
        every { userRepository.notExists(user.id) } returns true
        every { userRepository.save(ofType<User.DefinedUser>()) } returns user
        every { userRepository.exists(user.id) } returns false
        // Act
        val thrown = catchThrowable { createUserUseCase.unfold(CreateUserRequest(administrator.id, user)) }
        // Assert
        assertThat(thrown).isInstanceOf(UserCreationFailed::class.java).hasMessage("User with ID 123 was not created.")
    }

    @Test
    fun `should create draft user with the id assigned by the repository`() {
        // Arrange
        existing(administrator)
        val savedUser = DefinedUser(ValidUserId("generated-id"), draftUser.fullName, draftUser.email, PlainPassword("password123"))
        every { userRepository.emailIsAlreadyUsed(draftUser.email) } returns false
        every { userRepository.save(draftUser) } returns savedUser
        every { userRepository.notExists(savedUser.id) } returns false
        // Act
        createUserUseCase.unfold(CreateUserRequest(administrator.id, draftUser))
        // Assert
        assertThat(createUserUseCase.createdUser).isEqualTo(savedUser)
    }

    @Test
    fun `should fail if draft user email is already used`() {
        // Arrange
        existing(administrator)
        every { userRepository.emailIsAlreadyUsed(draftUser.email) } returns true
        // Act
        val thrown = catchThrowable { createUserUseCase.unfold(CreateUserRequest(administrator.id, draftUser)) }
        // Assert
        assertThat(thrown).isInstanceOf(UserAlreadyExists::class.java)
            .hasMessage("User with email john.doe@example.com already exists.")
    }

    @Test
    fun `should fail if draft user password is blank`() {
        // Arrange
        existing(administrator)
        val draftUserWithBlankPassword = draftUser.copy(password = PlainPassword(" "))
        every { userRepository.emailIsAlreadyUsed(draftUserWithBlankPassword.email) } returns false
        val createUserRequest = CreateUserRequest(administrator.id, draftUserWithBlankPassword)
        // Act
        val thrown = catchThrowable { createUserUseCase.unfold(createUserRequest) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidPasswordException::class.java).hasMessage("Password must not be blank.")
    }
}
