package org.ucop.domain

import org.flexstore.domain.shared.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.usecase.*
import org.flexstore.domain.user.model.User.DefinedUser
import org.flexstore.domain.user.model.UserId.ValidUserId
import io.mockk.spyk
import io.mockk.verify
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test

class UseCaseHandlerTest {

    @Test
    fun `should create and update user`() {
        // Given
        val userRepository = spyk<InMemoryUserRepository>()
        val user = DefinedUser(ValidUserId("123"), Name("Jane Doe"), Email("jane.doe@example.com"), PlainPassword("password123"))

        val createUserUseCase = CreateUserUseCase(userRepository)
        val updateUserUseCase = UpdateUserUseCase(userRepository)

        val createUserUcHandler = DefaultUseCaseHandler(createUserUseCase)
        val updateUserUcHandler = DefaultUseCaseHandler(updateUserUseCase)
        createUserUcHandler.setNext(updateUserUcHandler)
        // When
        createUserUcHandler.handle(user)
        // Then
        verify { userRepository.save(ofType<User.DefinedUser>()) }
    }
}