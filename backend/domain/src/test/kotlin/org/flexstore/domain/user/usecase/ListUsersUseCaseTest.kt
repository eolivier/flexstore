package org.flexstore.domain.user.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.junit.jupiter.api.Test

class ListUsersUseCaseTest : WithAssertions {

    private val useCase = ListUsersUseCase(usersOfTheAdministration())

    @Test
    fun `an administrator should read all the users`() {
        // Act
        val users = useCase.unfold(UsersReading(testAdministrator.id))
        // Assert
        assertThat(users.asList()).contains(testAdministrator, testCustomer)
    }

    @Test
    fun `a customer should not read the users`() {
        // Act
        val thrown = catchThrowable { useCase.unfold(UsersReading(testCustomer.id)) }
        // Assert
        assertThat(thrown).isInstanceOf(UserReadingForbiddenException::class.java)
    }
}
