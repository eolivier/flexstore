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

class CreateUserUseCaseTest {

    @Test
    fun `should create user successfully`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val user = DefinedUser(ValidUserId("123"), Name("John Doe"), Email("john.doe@example.com"), PlainPassword("password123"))
        
        every { userRepository.save(ofType<User.DefinedUser>()) } answers { firstArg() }
        every { userRepository.exists(user.id) } returns false
        every { userRepository.notExists(user.id) } returns false
        
        val createUserUseCase = CreateUserUseCase(userRepository)
        
        // Act
        createUserUseCase.unfold(user)
        
        // Assert
        verify { userRepository.notExists(user.id) }
        verify(exactly = 1) { userRepository.save(ofType<User.DefinedUser>()) }
        verify { userRepository.exists(user.id) }
    }

    @Test
    fun `should fail if user already exists`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val user = DefinedUser(ValidUserId("123"), Name("John Doe"), Email("john.doe@example.com"), PlainPassword("password123"))
        every { userRepository.exists(user.id) } returns true
        val createUserUseCase = CreateUserUseCase(userRepository)
        val exception = assertThrows<UserAlreadyExists> {
            // Act
            createUserUseCase.unfold(user)
        }
        // Assert
        assertEquals("User with ID 123 already exists.", exception.message)
    }

    @Test
    fun `should fail if user is not created`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val user = DefinedUser(ValidUserId("123"), Name("John Doe"), Email("john.doe@example.com"), PlainPassword("password123"))
        
        every { userRepository.notExists(user.id) } returns true
        every { userRepository.save(ofType<User.DefinedUser>()) } returns user
        every { userRepository.exists(user.id) } returns false
        
        // Act
        val createUserUseCase = CreateUserUseCase(userRepository)
        val exception = assertThrows<UserCreationFailed> {
            createUserUseCase.unfold(user)
        }
        // Assert
        assertEquals("User with ID 123 was not created.", exception.message)
    }

    @Test
    fun `should create draft user with the id assigned by the repository`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val draftUser = User.DraftUser(Name("John Doe"), Email("john.doe@example.com"), PlainPassword("password123"))
        val savedUser = DefinedUser(ValidUserId("generated-id"), draftUser.name, draftUser.email, PlainPassword("password123"))

        every { userRepository.notExistsBasedOn(draftUser.email) } returns true
        every { userRepository.save(draftUser) } returns savedUser
        every { userRepository.notExists(savedUser.id) } returns false

        val createUserUseCase = CreateUserUseCase(userRepository)

        // Act
        createUserUseCase.unfold(draftUser)

        // Assert
        assertEquals(savedUser, createUserUseCase.createdUser)
        verify { userRepository.notExists(savedUser.id) }
    }

    @Test
    fun `should fail if draft user email is already used`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val draftUser = User.DraftUser(Name("John Doe"), Email("john.doe@example.com"), PlainPassword("password123"))
        every { userRepository.notExistsBasedOn(draftUser.email) } returns false
        val createUserUseCase = CreateUserUseCase(userRepository)
        val exception = assertThrows<UserAlreadyExists> {
            // Act
            createUserUseCase.unfold(draftUser)
        }
        // Assert
        assertEquals("User with email john.doe@example.com already exists.", exception.message)
    }

    @Test
    fun `should fail if draft user password is blank`() {
        // Arrange
        val userRepository = mockk<UserRepository>()
        val draftUser = User.DraftUser(Name("John Doe"), Email("john.doe@example.com"), PlainPassword(" "))
        every { userRepository.notExistsBasedOn(draftUser.email) } returns true
        val createUserUseCase = CreateUserUseCase(userRepository)
        val exception = assertThrows<InvalidPasswordException> {
            // Act
            createUserUseCase.unfold(draftUser)
        }
        // Assert
        assertEquals("Password must not be blank.", exception.message)
    }
}
