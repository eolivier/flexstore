package org.flexstore.domain.loyalty.usecase

import org.flexstore.domain.actor.ActorResolutionService
import java.math.BigDecimal
import java.time.Instant
import org.assertj.core.api.WithAssertions
import org.flexstore.domain.cart.*
import org.flexstore.domain.loyalty.LoyaltyPoints
import org.flexstore.domain.loyalty.LoyaltyStatement
import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.customersWithIds
import org.flexstore.domain.user.model.AccountRefusedException
import org.flexstore.domain.user.model.AccountStatus
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.UserNotFoundException
import org.flexstore.domain.user.blockedCustomerId
import org.flexstore.domain.user.customerWithStatus
import org.flexstore.domain.user.unknownCustomerId
import org.flexstore.infra.repository.InMemoryOrderRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource

class ReadLoyaltyPointsUseCaseTest : WithAssertions {

    private val customerId = UserId.ValidUserId("user-1")
    private val customer = CartOwner.Customer(customerId)
    private val orderRepository = InMemoryOrderRepository()
    private val userRepository = customersWithIds(customerId.value)
    private val actorResolutionService = ActorResolutionService(userRepository)
    private val useCase = ReadLoyaltyPointsUseCase(orderRepository)

    private fun productPricedAt(amount: String) = DefinedProduct(
        ProductId(Identity.nextId().value), Name("Product"), Price(Amount(BigDecimal(amount)), Currency.EUR)
    )

    private fun placeOrder(
        product: DefinedProduct, quantity: Int, status: OrderStatus, placedAt: String = "2026-10-24T10:00:00Z"
    ): Order {
        val stamp = OrderStamp(OrderId.nextId(), Instant.parse(placedAt))
        val placement = OrderPlacement(stamp, Checkout(customer, homeOrderAddresses, acceptedTermsOfSale))
        val order = Order.place(placement, Cart(customer).add(product, Quantity(quantity)))
        return orderRepository.save(order.withStatus(status))
    }

    @Test
    fun `a customer should earn one point for each whole euro of their paid orders`() {
        // Arrange
        placeOrder(productPricedAt("35.00"), 2, OrderStatus.PAID)
        placeOrder(productPricedAt("19.99"), 1, OrderStatus.DELIVERED)
        // Act
        val statement = useCase.unfold(actorResolutionService.actorOf(customerId))
        // Assert: 70.00 (free shipping), then 19.99 + 5.00 of shipping = 24.99, without the cents
        assertThat(statement.total()).isEqualTo(LoyaltyPoints(70 + 24))
    }

    @Test
    fun `each paid order should be a line with its points, the newest first`() {
        // Arrange
        val older = placeOrder(productPricedAt("35.00"), 2, OrderStatus.PAID, "2026-10-01T10:00:00Z")
        val newer = placeOrder(productPricedAt("19.99"), 1, OrderStatus.DELIVERED, "2026-10-24T10:00:00Z")
        // Act
        val statement = useCase.unfold(actorResolutionService.actorOf(customerId))
        // Assert
        assertThat(statement.asList().map { it.orderNumber }).containsExactly(newer.number(), older.number())
        assertThat(statement.asList().map { it.points }).containsExactly(LoyaltyPoints(24), LoyaltyPoints(70))
    }

    @ParameterizedTest
    @EnumSource(OrderStatus::class, names = ["AWAITING_PAYMENT", "CANCELLED", "REFUNDED"])
    fun `an order that is not paid should not earn points`(status: OrderStatus) {
        // Arrange
        placeOrder(productPricedAt("50.00"), 1, status)
        // Act
        val statement = useCase.unfold(actorResolutionService.actorOf(customerId))
        // Assert
        assertThat(statement).isEqualTo(LoyaltyStatement(emptyList()))
        assertThat(statement.total()).isEqualTo(LoyaltyPoints(0))
    }

    @Test
    fun `a customer without any order should have no line and no point`() {
        // Act
        val statement = useCase.unfold(actorResolutionService.actorOf(customerId))
        // Assert
        assertThat(statement).isEqualTo(LoyaltyStatement(emptyList()))
        assertThat(statement.total()).isEqualTo(LoyaltyPoints(0))
    }

    @Test
    fun `an unknown customer should not read their points`() {
        // Act
        val thrown = catchThrowable { useCase.unfold(actorResolutionService.actorOf(unknownCustomerId)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UserNotFoundException::class.java)
    }

    @ParameterizedTest
    @EnumSource(AccountStatus::class, names = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `a customer whose account is refused should not read their points`(status: AccountStatus) {
        // Arrange
        userRepository.save(customerWithStatus(status))
        // Act
        val thrown = catchThrowable { useCase.unfold(actorResolutionService.actorOf(blockedCustomerId)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(AccountRefusedException::class.java)
    }
}
