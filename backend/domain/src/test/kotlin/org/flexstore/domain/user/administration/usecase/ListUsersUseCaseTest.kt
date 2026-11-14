package org.flexstore.domain.user.administration.usecase

import org.flexstore.domain.actor.*
import org.flexstore.domain.sharedkernel.PageRequest
import org.assertj.core.api.WithAssertions
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.junit.jupiter.api.Test
import org.flexstore.domain.user.administration.testAdministrator
import org.flexstore.domain.user.administration.testCustomer
import org.flexstore.domain.user.administration.usersOfTheAdministration

class ListUsersUseCaseTest : WithAssertions {

    private val useCase = ListUsersUseCase(usersOfTheAdministration())

    @Test
    fun `an administrator should read all the users`() {
        // Act
        val page = useCase.unfold(UsersReading(testAdministratorActor, PageRequest.first()))
        // Assert
        assertThat(page.content.asList()).contains(testAdministrator, testCustomer)
    }

    @Test
    fun `a customer should not read the users`() {
        // Act
        val reading = UsersReading(testCustomerActor, PageRequest.first())
        val thrown = catchThrowable { useCase.unfold(reading) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserReadingForbiddenException::class.java)
    }

    @Test
    fun `an administrator should read a page of the users with the total of all the users`() {
        // Act
        val page = useCase.unfold(UsersReading(testAdministratorActor, PageRequest.of(0, 1)))
        // Assert
        assertThat(page.content.asList()).hasSize(1)
        assertThat(page.total.value).isGreaterThanOrEqualTo(2)
    }

    @Test
    fun `the pages of the users should hold each user once`() {
        // Arrange
        val total = useCase.unfold(UsersReading(testAdministratorActor, PageRequest.first())).total.value.toInt()
        // Act
        val users = (0 until total).flatMap { number ->
            useCase.unfold(UsersReading(testAdministratorActor, PageRequest.of(number, 1))).content.asList()
        }
        // Assert
        assertThat(users).doesNotHaveDuplicates().hasSize(total)
    }

    @Test
    fun `an administrator should find the users whose name or email matches every word searched`() {
        // Arrange
        val usersSearch = UsersSearch(UserSearchText("TEST admin"), PageRequest.first())
        // Act
        val page = useCase.unfold(UsersReading(testAdministratorActor, usersSearch))
        // Assert
        assertThat(page.content.asList()).allSatisfy { user ->
            assertThat(UserSearchText("TEST admin").matches(user as User.DefinedUser)).isTrue()
        }
        assertThat(page.total.value).isEqualTo(page.content.asList().size.toLong())
    }

    @Test
    fun `an administrator should find no user when no user matches the search`() {
        // Arrange
        val usersSearch = UsersSearch(UserSearchText("nobody-has-this-name"), PageRequest.first())
        // Act
        val page = useCase.unfold(UsersReading(testAdministratorActor, usersSearch))
        // Assert
        assertThat(page.content.asList()).isEmpty()
        assertThat(page.total.value).isZero()
    }

    @Test
    fun `a customer should not search the users`() {
        // Arrange
        val usersSearch = UsersSearch(UserSearchText("test"), PageRequest.first())
        // Act
        val thrown = catchThrowable { useCase.unfold(UsersReading(testCustomerActor, usersSearch)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserReadingForbiddenException::class.java)
    }
}
