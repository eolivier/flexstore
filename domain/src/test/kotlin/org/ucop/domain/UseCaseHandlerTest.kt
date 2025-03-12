package org.ucop.domain

import io.mockk.every
import io.mockk.mockk
import org.flexstore.domain.entity.Email
import org.flexstore.domain.entity.User.DefinedUser
import org.flexstore.domain.entity.UserId.ValidUserId
import org.flexstore.domain.repository.UserRepository
import org.flexstore.domain.usecase.CreateUserUseCase
import org.flexstore.domain.usecase.UpdateUserUseCase
import org.ucop.domain.entity.Name

class UseCaseHandlerTest {

    @org.junit.jupiter.api.Test
    fun `should create and update user`() {
        // Create a mock instance of UserRepository
        val userRepository = mockk<UserRepository>()

        // Define the behavior of the mock
        val user = DefinedUser(ValidUserId("123"), Name("Jane Doe"), Email("jane.doe@example.com"))
        every { userRepository.notExists(user.id) } returns true
        every { userRepository.save(user) } returns Unit
        every { userRepository.exists(user.id) } returns true

        // Create an instance of CreateUserUseCase with the mock repository
        val createUserUseCase = CreateUserUseCase(userRepository)
        val updateUserUseCase = UpdateUserUseCase(userRepository)

        val createUserUcHandler = DefaultUseCaseHandler(createUserUseCase)
        val updateUserUcHandler = DefaultUseCaseHandler(updateUserUseCase)
        createUserUcHandler.setNext(updateUserUcHandler)

        createUserUcHandler.handle(user)
    }
}