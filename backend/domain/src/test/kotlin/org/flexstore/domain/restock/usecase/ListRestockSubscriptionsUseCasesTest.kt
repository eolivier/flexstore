package org.flexstore.domain.restock.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.actor.*
import org.flexstore.domain.product.model.ProductId
import org.flexstore.domain.restock.*
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.stock.Stock
import org.flexstore.domain.stock.StockQuantity
import org.flexstore.domain.user.customersWithIds
import org.flexstore.domain.user.model.AccountStatus
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.UserNotFoundException
import org.flexstore.domain.user.unknownCustomerId
import org.flexstore.infra.repository.InMemoryRestockSubscriptionRepository
import org.flexstore.infra.repository.InMemoryStockRepository
import org.junit.jupiter.api.Test
import org.flexstore.domain.sharedkernel.Name
import java.time.Instant

class ListRestockSubscriptionsUseCasesTest : WithAssertions {

    private val restockedLamp = ProductId(Identity("desk-lamp"))
    private val missingMug = ProductId(Identity("mug"))
    private val jane = UserId.ValidUserId("user-1")
    private val john = UserId.ValidUserId("user-2")
    private val subscribedAt = SubscribedAt(Instant.parse("2026-11-05T10:00:00Z"))
    private val janeWaitsForTheLamp = subscriptionOf(jane, restockedLamp)
    private val janeWaitsForTheMug = subscriptionOf(jane, missingMug)
    private val johnWaitsForTheLamp = subscriptionOf(john, restockedLamp)
    private fun subscriptionOf(customer: UserId.ValidUserId, productId: ProductId) =
        RestockSubscription(RestockSubscriptionId.nextId(), customer, productId, subscribedAt)

    private val restockSubscriptionRepository = InMemoryRestockSubscriptionRepository().apply {
        save(janeWaitsForTheLamp)
        save(janeWaitsForTheMug)
        save(johnWaitsForTheLamp)
    }
    private val stockRepository = InMemoryStockRepository().apply { save(Stock(restockedLamp, StockQuantity(4))) }
    private val actorResolutionService = ActorResolutionService(customersWithIds("user-1", "user-2"))
    private val listCustomerUseCase = ListCustomerRestockSubscriptionsUseCase(restockSubscriptionRepository)
    private val listRestockedUseCase = ListRestockedSubscriptionsUseCase(restockSubscriptionRepository, stockRepository)

    @Test
    fun `should list the subscriptions of the customer only`() {
        // Act
        val subscriptions = listCustomerUseCase.unfold(actorResolutionService.actorOf(jane))
        // Assert
        assertThat(subscriptions.asList()).containsExactlyInAnyOrder(janeWaitsForTheLamp, janeWaitsForTheMug)
    }

    @Test
    fun `an unknown customer should not list subscriptions`() {
        // Act
        val thrown = catchThrowable { listCustomerUseCase.unfold(actorResolutionService.actorOf(unknownCustomerId)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `should list the subscriptions whose product is back in stock`() {
        // Act
        val subscriptions = listRestockedUseCase.unfold(RestockedSubscriptionsReading(SystemActor()))
        // Assert
        assertThat(subscriptions.asList()).containsExactlyInAnyOrder(janeWaitsForTheLamp, johnWaitsForTheLamp)
    }

    @Test
    fun `an administrator should list the subscriptions whose product is back in stock`() {
        // Arrange
        val administrator = Admin(jane, Name("Jane Doe"), AccountStatus.ACTIVE)
        // Act
        val subscriptions = listRestockedUseCase.unfold(RestockedSubscriptionsReading(administrator))
        // Assert
        assertThat(subscriptions.asList()).hasSize(2)
    }

    @Test
    fun `a customer should not list the subscriptions whose product is back in stock`() {
        // Arrange
        val reading = RestockedSubscriptionsReading(actorResolutionService.actorOf(jane))
        // Act
        val thrown = catchThrowable { listRestockedUseCase.unfold(reading) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(RestockNotificationForbiddenException::class.java)
    }
}
