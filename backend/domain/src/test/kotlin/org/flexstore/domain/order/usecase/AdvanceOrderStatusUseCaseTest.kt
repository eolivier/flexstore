package org.flexstore.domain.order.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.flexstore.infra.repository.InMemoryOrderRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import java.time.Instant

class AdvanceOrderStatusUseCaseTest : WithAssertions {

    private val price = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), price)
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val orderId = OrderId("order-1")
    private val orderRepository = InMemoryOrderRepository()
    private val now = Instant.parse("2026-10-25T09:30:00Z")
    private val useCase = AdvanceOrderStatusUseCase(orderRepository)

    // The status changes at a known moment
    @BeforeEach
    fun fixTheMoment() {
        InstantProvider.fixAt(now)
    }

    @AfterEach
    fun resetTheMoment() {
        InstantProvider.reset()
    }

    private fun storeOrder(status: OrderStatus) {
        val orderStamp = OrderStamp(orderId, Instant.parse("2026-10-24T10:00:00Z"))
        val orderPlacement = OrderPlacement(orderStamp, Checkout(customer, homeOrderAddresses))
        val order = Order.place(orderPlacement, Cart(customer).add(deskLamp, Quantity(1)))
        orderRepository.save(order.withStatus(status))
    }

    private fun storedStatus() = orderRepository.findById(orderId)?.status

    @Test
    fun `the order should keep the moment it reached its next status`() {
        // Arrange
        storeOrder(OrderStatus.PAID)
        // Act
        useCase.unfold(OrderStatusAdvance(orderId, OrderStatus.PAID))
        // Assert
        val lastChange = orderRepository.findById(orderId)!!.history.asList().last()
        assertThat(lastChange).isEqualTo(OrderStatusChange(OrderStatus.PROCESSING, StatusChangedAt(now)))
    }

    @ParameterizedTest(name = "an order {0} should become {1}")
    @CsvSource("PAID, PROCESSING", "PROCESSING, SHIPPED", "SHIPPED, DELIVERED")
    fun `an order should move to the next status of its life cycle`(status: OrderStatus, next: OrderStatus) {
        // Arrange
        storeOrder(status)
        // Act
        useCase.unfold(OrderStatusAdvance(orderId, status))
        // Assert
        assertThat(storedStatus()).isEqualTo(next)
    }

    @ParameterizedTest(name = "an order {0} should not be moved by an administrator")
    @CsvSource("AWAITING_PAYMENT", "DELIVERED")
    fun `an order awaiting its payment or delivered should not be moved`(status: OrderStatus) {
        // Arrange
        storeOrder(status)
        // Act
        val thrown = catchThrowable { useCase.unfold(OrderStatusAdvance(orderId, status)) }
        // Assert
        assertThat(thrown).isInstanceOf(OrderStatusCannotAdvanceException::class.java)
        assertThat(storedStatus()).isEqualTo(status)
    }

    @Test
    fun `an order whose status changed since it was seen should not move`() {
        // Arrange
        storeOrder(OrderStatus.SHIPPED)
        // Act
        val thrown = catchThrowable { useCase.unfold(OrderStatusAdvance(orderId, OrderStatus.PROCESSING)) }
        // Assert
        assertThat(thrown).isInstanceOf(OrderStatusChangedException::class.java)
        assertThat(storedStatus()).isEqualTo(OrderStatus.SHIPPED)
    }

    @Test
    fun `an unknown order should not be found`() {
        // Act
        val thrown = catchThrowable { useCase.unfold(OrderStatusAdvance(orderId, OrderStatus.PROCESSING)) }
        // Assert
        assertThat(thrown).isInstanceOf(OrderNotFoundException::class.java)
    }
}
