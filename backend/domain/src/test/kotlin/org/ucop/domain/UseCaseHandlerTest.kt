package org.ucop.domain

import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.usecase.*
import org.flexstore.domain.user.model.User.DefinedUser
import org.flexstore.domain.user.model.UserId.ValidUserId
import io.mockk.spyk
import io.mockk.verify
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test
import org.ucop.domain.entity.*
import org.flexstore.domain.user.administration.usecase.ReadUserUseCase
import org.flexstore.domain.user.administration.usecase.UserReading

// A use case chained after the reading of a user, on the same request: it deletes the user read
private class ForgetReadUserUseCase(private val userRepository: InMemoryUserRepository) : UseCase<UserReading> {
    override fun getPreConditions(): PreConditions<UserReading> = PreConditions.none()

    override fun getNominalScenario(): NominalScenario<UserReading> =
        NominalScenario(Step { reading -> userRepository.delete(reading.userId) })

    override fun getPostConditions(): PostConditions<UserReading> = PostConditions.none()
}

class UseCaseHandlerTest {

    @Test
    fun `should run the chained use cases whose preconditions are met`() {
        // Arrange
        val userRepository = spyk<InMemoryUserRepository>()
        val user = DefinedUser(ValidUserId("123"), FullName(FirstName("Jane"), LastName("Doe")), Email("jane.doe@example.com"), PlainPassword("password123"))
        val administrator = DefinedUser(
            ValidUserId("admin-1"), FullName(FirstName("Ada"), LastName("Admin")), Email("admin@example.com"),
            PlainPassword("password123"), Role.ADMIN
        )
        userRepository.save(user)
        userRepository.save(administrator)
        val readUserUcHandler = DefaultUseCaseHandler(ReadUserUseCase(userRepository))
        readUserUcHandler.setNext(DefaultUseCaseHandler(ForgetReadUserUseCase(userRepository)))
        // Act
        readUserUcHandler.handle(UserReading(administrator.id, user.id))
        // Assert
        verify { userRepository.findById(user.id) }
        verify { userRepository.delete(user.id) }
    }
}
