package org.flexstore.domain.user

import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.model.User.DefinedUser
import org.flexstore.domain.user.model.UserId.ValidUserId
import org.assertj.core.api.WithAssertions
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test

class IsAdministratorPreConditionTest : WithAssertions {

    // The input of a use case reserved to the administrators: the actor asks for an action
    private data class Request(val actor: UserId)

    private val administrator = userOf("admin-1", Role.ADMIN)
    private val customer = userOf("customer-1", Role.USER)
    private val userRepository = InMemoryUserRepository().apply {
        save(administrator)
        save(customer)
    }
    private val isAdministratorPreCondition =
        IsAdministratorPreCondition<Request>(userRepository) { request -> request.actor }

    private fun userOf(id: String, role: Role) = DefinedUser(
        ValidUserId(id), FullName(FirstName("Jane"), LastName("Doe")), Email("$id@flexstore.fr"),
        HashedPassword("hashed"), role
    )

    @Test
    fun `an administrator should be accepted`() {
        // Act
        val thrown = catchThrowable { isAdministratorPreCondition.validate(Request(administrator.id)) }
        // Assert
        assertThat(thrown).isNull()
    }

    @Test
    fun `a customer should be refused`() {
        // Act
        val thrown = catchThrowable { isAdministratorPreCondition.validate(Request(customer.id)) }
        // Assert
        assertThat(thrown).isInstanceOf(ActorIsNotAdministratorException::class.java)
    }

    @Test
    fun `an unknown user should be refused`() {
        // Act
        val thrown = catchThrowable { isAdministratorPreCondition.validate(Request(ValidUserId("unknown"))) }
        // Assert
        assertThat(thrown).isInstanceOf(ActorIsNotAdministratorException::class.java)
    }
}
