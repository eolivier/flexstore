package org.flexstore.domain.cart.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.actor.*
import org.flexstore.domain.cart.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.UserId
import org.flexstore.infra.repository.InMemoryCartRepository
import org.flexstore.domain.user.model.AccountStatus
import org.junit.jupiter.api.Test
import java.time.Instant

class ListAbandonedCartsUseCaseTest : WithAssertions {

    private val useCase = ListAbandonedCartsUseCase(InMemoryCartRepository())
    private val limit = AbandonmentLimit(Instant.parse("2026-10-25T10:00:00Z"))
    private val userId = UserId.ValidUserId("user-1")
    private val actorName = Name("Jane Doe")

    @Test
    fun `the system should read the abandoned carts`() {
        // Act
        val abandonedCarts = useCase.unfold(AbandonedCartsReading(SystemActor(), limit))
        // Assert
        assertThat(abandonedCarts.isEmpty()).isTrue()
    }

    @Test
    fun `an administrator should read the abandoned carts`() {
        // Arrange
        val admin = Admin(userId, actorName, AccountStatus.ACTIVE)
        // Act
        val abandonedCarts = useCase.unfold(AbandonedCartsReading(admin, limit))
        // Assert
        assertThat(abandonedCarts.isEmpty()).isTrue()
    }

    @Test
    fun `a customer should not read the abandoned carts`() {
        // Act
        val thrown = catchThrowable {
            useCase.unfold(AbandonedCartsReading(Customer(userId, actorName, AccountStatus.ACTIVE), limit))
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(AbandonedCartForbiddenException::class.java)
    }

    @Test
    fun `a visitor should not read the abandoned carts`() {
        // Act
        val thrown = catchThrowable { useCase.unfold(AbandonedCartsReading(Visitor(), limit)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(AbandonedCartForbiddenException::class.java)
    }
}
