package org.flexstore.domain.actor

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.sharedkernel.Name
import org.flexstore.domain.user.model.AccountStatus
import org.flexstore.domain.user.model.UserId.ValidUserId
import org.junit.jupiter.api.Test
import org.ucop.domain.entity.Actor
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class ActorsTest : WithAssertions {

    private val admin = Admin(ValidUserId("admin-1"), Name("Ada Admin"), AccountStatus.ACTIVE)
    private val customer = Customer(ValidUserId("customer-1"), Name("Jane Doe"), AccountStatus.ACTIVE)
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
    fun `an admin, a customer and a visitor should be at least a visitor`() {
        // Act
        val atLeastVisitors = actors.filter { it.isAtLeastVisitor() }
        // Assert
        assertThat(atLeastVisitors).containsExactly(admin, customer, visitor)
    }

    @Test
    fun `every actor should read the public content, the system included`() {
        // Act
        val readers = actors.filter { it.canReadPublicContent() }
        // Assert
        assertThat(readers).containsExactlyElementsOf(actors)
    }

    @Test
    fun `an actor unknown to the shop should not read the public content`() {
        // Arrange
        val stranger = Actor(Name("Stranger"))
        // Act - Assert
        assertThat(stranger.cannotReadPublicContent()).isTrue()
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

    @Test
    fun `a visitor and the system should not be at least a customer`() {
        // Act
        val notAtLeastCustomers = actors.filter { it.isNotAtLeastCustomer() }
        // Assert
        assertThat(notAtLeastCustomers).containsExactly(visitor, systemActor)
    }

    @ParameterizedTest(name = "an account {0} lets the customer act")
    @CsvSource("PENDING_VERIFICATION", "ACTIVE")
    fun `a customer whose account is neither suspended, locked nor disabled should act`(status: AccountStatus) {
        // Arrange
        val customerOfStatus = Customer(ValidUserId("customer-2"), Name("Jane Doe"), status)
        // Act
        val canAct = customerOfStatus.canAct()
        // Assert
        assertThat(canAct).isTrue()
    }

    @ParameterizedTest(name = "an account {0} does not let the customer act")
    @CsvSource("SUSPENDED", "LOCKED", "DISABLED")
    fun `a customer whose account is suspended, locked or disabled should not act`(status: AccountStatus) {
        // Arrange
        val customerOfStatus = Customer(ValidUserId("customer-2"), Name("Jane Doe"), status)
        // Act
        val cannotAct = customerOfStatus.cannotAct()
        // Assert
        assertThat(cannotAct).isTrue()
    }

    @Test
    fun `an admin should tell whether it can act like a customer does`() {
        // Arrange
        val suspendedAdmin = Admin(ValidUserId("admin-2"), Name("Ada Admin"), AccountStatus.SUSPENDED)
        // Act
        val canAct = suspendedAdmin.canAct()
        // Assert
        assertThat(canAct).isFalse()
    }

    @Test
    fun `an actor should be the user of its own account only`() {
        // Act
        val adminAccounts = actors.filter { it.isTheUser(ValidUserId("admin-1")) }
        // Assert
        assertThat(adminAccounts).containsExactly(admin)
    }
}
