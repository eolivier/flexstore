package org.ucop.domain

import io.mockk.every
import io.mockk.mockk
import org.flexstore.domain.entity.Email
import org.flexstore.domain.entity.User
import org.flexstore.domain.entity.UserId
import org.flexstore.domain.repository.UserRepository
import org.flexstore.domain.usecase.CreateUserUseCase
import org.flexstore.domain.usecase.UpdateUserUseCase
import org.ucop.domain.entity.Name

class UseCaseHandlerTest {

    @org.junit.jupiter.api.Test
    fun testSetNext() {
        // Create a mock instance of UserRepository
        val userRepository = mockk<UserRepository>()

        // Define the behavior of the mock
        val user = User(UserId("123"), Name("Jane Doe"), Email("jane.doe@example.com"))
        every { userRepository.notExists(user.id) } returns true
        every { userRepository.save(user) } returns Unit
        every { userRepository.exists(user.id) } returns true

        // Create an instance of CreateUserUseCase with the mock repository
        val createUserUseCase = CreateUserUseCase(userRepository)
        val updateUserUseCase = UpdateUserUseCase(userRepository)

        val createUserUcHandler = BaseUseCaseHandler(createUserUseCase)
        val updateUserUcHandler = BaseUseCaseHandler(updateUserUseCase)
        createUserUcHandler.setNext(updateUserUcHandler)

        createUserUcHandler.handle(user)
    }
}