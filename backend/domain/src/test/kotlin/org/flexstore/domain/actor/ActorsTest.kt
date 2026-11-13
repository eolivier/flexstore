package org.flexstore.domain.actor

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.sharedkernel.Name
import org.flexstore.domain.user.model.UserId.ValidUserId
import org.junit.jupiter.api.Test

class ActorsTest : WithAssertions {

    private val admin = Admin(ValidUserId("admin-1"), Name("Ada Admin"))
    private val customer = Customer(ValidUserId("customer-1"), Name("Jane Doe"))
    private val visitor = Visitor()
    private val systemActor = SystemActor()
    private val actors = listOf(admin, customer, visitor, systemActor)

    @Test
    fun `only an admin should be an admin`() {
        // Act
        val admins = actors.filter { it.isAdmin() }
        // Assert
        assertThat(admins).containsExactly(admin)
    }

    @Test
    fun `only a customer should be exactly a customer`() {
        // Act
        val customers = actors.filter { it.isCustomer() }
        // Assert
        assertThat(customers).containsExactly(customer)
    }

    @Test
    fun `an admin and a customer should be at least a customer`() {
        // Act
        val atLeastCustomers = actors.filter { it.isAtLeastCustomer() }
        // Assert
        assertThat(atLeastCustomers).containsExactly(admin, customer)
    }

    @Test
    fun `only a visitor should be exactly a visitor`() {
        // Act
        val visitors = actors.filter { it.isVisitor() }
        // Assert
        assertThat(visitors).containsExactly(visitor)
    }

    @Test
    fun `everyone but an admin should not be an admin`() {
        // Act
        val notAdmins = actors.filter { it.isNotAdmin() }
        // Assert
        assertThat(notAdmins).containsExactly(customer, visitor, systemActor)
    }
}
