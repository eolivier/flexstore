package org.flexstore.domain.usecase

import org.junit.jupiter.api.Assertions.*
import org.flexstore.domain.*
import org.flexstore.domain.entity.Email
import org.flexstore.domain.entity.User
import org.flexstore.domain.entity.UserId
import org.flexstore.domain.repository.UserRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.ucop.domain.entity.Name

class CreateUserDeprecatedUseCaseTest {

    private lateinit var userRepository: UserRepository
    private lateinit var createUserUseCase: DeprecatedCreateUserUseCase

    @BeforeEach
    fun setUp() {
        userRepository = mock(UserRepository::class.java)
        createUserUseCase = DeprecatedCreateUserUseCase(userRepository)
    }

    @Test
    fun `should create user successfully`() {
        val user = User(UserId("123"), Name("John Doe"), Email("2@2.fr"))
        `when`(userRepository.notExists(user.id)).thenReturn(true)
        `when`(userRepository.exists(user.id)).thenReturn(true)

        createUserUseCase.perform(user)

        verify(userRepository).save(user)
        verify(userRepository).notExists(user.id)
        verify(userRepository).exists(user.id)
    }

    @Test
    fun `should throw exception if user already exists`() {
        val user = User(UserId("123"), Name("John Doe"), Email("2@2.fr"))

        `when`(userRepository.notExists(user.id)).thenReturn(false)

        val exception = assertThrows<AssertionError> {
            createUserUseCase.perform(user)
        }

        assertEquals("User with ID 123 already exists.", exception.message)
        verify(userRepository).notExists(user.id)
        verify(userRepository, never()).save(user)
    }
}