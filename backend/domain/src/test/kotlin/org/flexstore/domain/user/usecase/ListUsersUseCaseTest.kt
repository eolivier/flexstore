package org.flexstore.domain.user.usecase

import org.flexstore.domain.sharedkernel.PageRequest
import org.assertj.core.api.WithAssertions
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.junit.jupiter.api.Test

class ListUsersUseCaseTest : WithAssertions {

    private val useCase = ListUsersUseCase(usersOfTheAdministration())

    @Test
    fun `an administrator should read all the users`() {
        // Act
        val page = useCase.unfold(UsersReading(testAdministrator.id, PageRequest.first()))
        // Assert
        assertThat(page.content.asList()).contains(testAdministrator, testCustomer)
    }

    @Test
    fun `a customer should not read the users`() {
        // Act
        val reading = UsersReading(testCustomer.id, PageRequest.first())
        val thrown = catchThrowable { useCase.unfold(reading) }
        // Assert
        assertThat(thrown).isInstanceOf(UserReadingForbiddenException::class.java)
    }

    @Test
    fun `an administrator should read a page of the users with the total of all the users`() {
        // Act
        val page = useCase.unfold(UsersReading(testAdministrator.id, PageRequest.of(0, 1)))
        // Assert
        assertThat(page.content.asList()).hasSize(1)
        assertThat(page.total.value).isGreaterThanOrEqualTo(2)
    }

    @Test
    fun `the pages of the users should hold each user once`() {
        // Arrange
        val total = useCase.unfold(UsersReading(testAdministrator.id, PageRequest.first())).total.value.toInt()
        // Act
        val users = (0 until total).flatMap { number ->
            useCase.unfold(UsersReading(testAdministrator.id, PageRequest.of(number, 1))).content.asList()
        }
        // Assert
        assertThat(users).doesNotHaveDuplicates().hasSize(total)
    }
}
