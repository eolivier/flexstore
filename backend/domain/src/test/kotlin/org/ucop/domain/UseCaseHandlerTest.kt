package org.ucop.domain

import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.*
import org.flexstore.domain.user.usecase.*
import org.flexstore.domain.user.User.DefinedUser
import org.flexstore.domain.user.UserId.ValidUserId
import io.mockk.spyk
import io.mockk.verify
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test

class UseCaseHandlerTest {

    @Test
    fun `should run the chained use cases whose preconditions are met`() {
        // Arrange
        val userRepository = spyk<InMemoryUserRepository>()
        val user = DefinedUser(ValidUserId("123"), FullName(FirstName("Jane"), LastName("Doe")), Email("jane.doe@example.com"), PlainPassword("password123"))
        userRepository.save(user)
        val readUserUcHandler = DefaultUseCaseHandler(ReadUserUseCase(userRepository))
        readUserUcHandler.setNext(DefaultUseCaseHandler(DeleteUserUseCase(userRepository)))
        // Act
        readUserUcHandler.handle(user.id)
        // Assert
        verify { userRepository.findById(user.id) }
        verify { userRepository.delete(user.id) }
    }
}
