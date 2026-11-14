package org.flexstore.domain.restock.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.product.model.*
import org.flexstore.domain.restock.RestockSubscribing
import org.flexstore.domain.restock.RestockSubscriptionRefusedException
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.stock.Stock
import org.flexstore.domain.stock.StockQuantity
import org.flexstore.domain.user.blockedCustomerId
import org.flexstore.domain.user.customerWithStatus
import org.flexstore.domain.user.customersWithIds
import org.flexstore.domain.user.model.AccountRefusedException
import org.flexstore.domain.user.model.AccountStatus
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.UserNotFoundException
import org.flexstore.domain.user.unknownCustomerId
import org.flexstore.infra.repository.InMemoryProductRepository
import org.flexstore.infra.repository.InMemoryRestockSubscriptionRepository
import org.flexstore.infra.repository.InMemoryStockRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.math.BigDecimal

class SubscribeToRestockUseCaseTest : WithAssertions {

    private val deskLamp = DefinedProduct(
        ProductId(Identity("desk-lamp")), Name("Desk lamp"), Price(Amount(BigDecimal(35)), Currency.EUR)
    )
    private val customer = UserId.ValidUserId("user-1")
    private val productRepository = InMemoryProductRepository().apply { save(deskLamp) }
    private val stockRepository = InMemoryStockRepository()
    private val restockSubscriptionRepository = InMemoryRestockSubscriptionRepository()
    private val userRepository = customersWithIds(customer.value, "user-2")
    private val actorResolutionService = ActorResolutionService(userRepository)
    private val useCase =
        SubscribeToRestockUseCase(restockSubscriptionRepository, productRepository, stockRepository)

    @Test
    fun `should subscribe the customer to the return of a product out of stock`() {
        // Arrange
        val subscribing = RestockSubscribing(actorResolutionService.actorOf(customer), deskLamp.productId)
        // Act
        useCase.unfold(subscribing)
        // Assert
        assertThat(restockSubscriptionRepository.contains(customer, deskLamp.productId)).isTrue()
    }

    @Test
    fun `subscribing again should leave the subscriptions as they are`() {
        // Arrange
        val subscribing = RestockSubscribing(actorResolutionService.actorOf(customer), deskLamp.productId)
        useCase.unfold(subscribing)
        // Act
        useCase.unfold(subscribing)
        // Assert
        assertThat(restockSubscriptionRepository.findAllBy(customer).asList()).hasSize(1)
    }

    @Test
    fun `should subscribe the customer concerned only`() {
        // Arrange
        val otherCustomer = UserId.ValidUserId("user-2")
        // Act
        useCase.unfold(RestockSubscribing(actorResolutionService.actorOf(customer), deskLamp.productId))
        // Assert
        assertThat(restockSubscriptionRepository.contains(otherCustomer, deskLamp.productId)).isFalse()
    }

    @Test
    fun `should not subscribe to a product in stock`() {
        // Arrange
        stockRepository.save(Stock(deskLamp.productId, StockQuantity(3)))
        val subscribing = RestockSubscribing(actorResolutionService.actorOf(customer), deskLamp.productId)
        // Act
        val thrown = catchThrowable { useCase.unfold(subscribing) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(RestockSubscriptionRefusedException::class.java)
        assertThat(restockSubscriptionRepository.contains(customer, deskLamp.productId)).isFalse()
    }

    @Test
    fun `should not subscribe to a product missing from the catalog`() {
        // Arrange
        val unknownProduct = ProductId(Identity("unknown"))
        val subscribing = RestockSubscribing(actorResolutionService.actorOf(customer), unknownProduct)
        // Act
        val thrown = catchThrowable { useCase.unfold(subscribing) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(ProductNotFoundException::class.java)
        assertThat(restockSubscriptionRepository.findAll().isEmpty()).isTrue()
    }

    @Test
    fun `an unknown customer should not subscribe`() {
        // Arrange
        val subscribing = RestockSubscribing(actorResolutionService.actorOf(unknownCustomerId), deskLamp.productId)
        // Act
        val thrown = catchThrowable { useCase.unfold(subscribing) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserNotFoundException::class.java)
    }

    @ParameterizedTest(name = "a customer whose account is {0} should not subscribe")
    @EnumSource(AccountStatus::class, names = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `a customer whose account is refused should not subscribe`(status: AccountStatus) {
        // Arrange
        userRepository.save(customerWithStatus(status))
        val subscribing = RestockSubscribing(actorResolutionService.actorOf(blockedCustomerId), deskLamp.productId)
        // Act
        val thrown = catchThrowable { useCase.unfold(subscribing) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(AccountRefusedException::class.java)
    }
}
