package org.flexstore.domain.restock

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.product.model.*
import org.flexstore.domain.restock.usecase.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.stock.Stock
import org.flexstore.domain.stock.StockQuantity
import org.flexstore.domain.user.customerWithStatus
import org.flexstore.domain.user.blockedCustomerId
import org.flexstore.domain.user.model.AccountStatus
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.usersWithSystemAndCustomers
import org.flexstore.infra.repository.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class RestockNotificationServiceTest : WithAssertions {

    private val deskLamp = DefinedProduct(
        ProductId(Identity("desk-lamp")), Name("Desk lamp"), Price(Amount(BigDecimal(35)), Currency.EUR)
    )
    private val jane = UserId.ValidUserId("user-1")
    private val john = UserId.ValidUserId("user-2")
    private val productRepository = InMemoryProductRepository().apply { save(deskLamp) }
    private val stockRepository = InMemoryStockRepository()
    private val restockSubscriptionRepository = InMemoryRestockSubscriptionRepository()
    private val userRepository = usersWithSystemAndCustomers("user-1", "user-2")
    private val emailRepository = InMemoryEmailRepository()
    private val service = RestockNotificationService(
        ActorResolutionService(userRepository),
        RestockUseCases(
            SubscribeToRestockUseCase(restockSubscriptionRepository, productRepository, stockRepository),
            UnsubscribeFromRestockUseCase(restockSubscriptionRepository),
            ListCustomerRestockSubscriptionsUseCase(restockSubscriptionRepository),
            ListRestockedSubscriptionsUseCase(restockSubscriptionRepository, stockRepository),
            NotifyRestockUseCase(
                restockSubscriptionRepository, productRepository, stockRepository, userRepository, emailRepository
            )
        )
    )

    private fun waitFor(customer: UserId.ValidUserId) {
        val subscribedAt = SubscribedAt(Instant.parse("2026-11-05T10:00:00Z"))
        val subscriptionId = RestockSubscriptionId.nextId()
        val subscription = RestockSubscription(subscriptionId, customer, deskLamp.productId, subscribedAt)
        restockSubscriptionRepository.save(subscription)
    }

    private fun bringBackInStock() = stockRepository.save(Stock(deskLamp.productId, StockQuantity(10)))

    @Test
    fun `should notify every customer who waits for the product back in stock`() {
        // Arrange
        waitFor(jane)
        waitFor(john)
        bringBackInStock()
        // Act
        val notifiedSubscriptions = service.notifyRestockedProducts()
        // Assert
        assertThat(notifiedSubscriptions.asList()).extracting("customer").containsExactlyInAnyOrder(jane, john)
        assertThat(emailRepository.sentRestockEmails()).hasSize(2)
    }

    @Test
    fun `should not notify while the product is out of stock`() {
        // Arrange
        waitFor(jane)
        // Act
        val notifiedSubscriptions = service.notifyRestockedProducts()
        // Assert
        assertThat(notifiedSubscriptions.isEmpty()).isTrue()
        assertThat(emailRepository.sentRestockEmails()).isEmpty()
        assertThat(restockSubscriptionRepository.contains(jane, deskLamp.productId)).isTrue()
    }

    @Test
    fun `should notify a customer once`() {
        // Arrange
        waitFor(jane)
        bringBackInStock()
        service.notifyRestockedProducts()
        // Act
        val notifiedSubscriptions = service.notifyRestockedProducts()
        // Assert
        assertThat(notifiedSubscriptions.isEmpty()).isTrue()
        assertThat(emailRepository.sentRestockEmails()).hasSize(1)
    }

    @Test
    fun `should leave aside a customer whose account cannot act and notify the others`() {
        // Arrange
        userRepository.save(customerWithStatus(AccountStatus.SUSPENDED))
        waitFor(blockedCustomerId)
        waitFor(jane)
        bringBackInStock()
        // Act
        val notifiedSubscriptions = service.notifyRestockedProducts()
        // Assert
        assertThat(notifiedSubscriptions.asList()).extracting("customer").containsExactly(jane)
        assertThat(restockSubscriptionRepository.contains(blockedCustomerId, deskLamp.productId)).isTrue()
    }
}
