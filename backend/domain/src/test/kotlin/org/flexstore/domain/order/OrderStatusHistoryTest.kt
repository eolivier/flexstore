package org.flexstore.domain.order

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class OrderStatusHistoryTest : WithAssertions {

    private val price = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), price)
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val placedAt = Instant.parse("2026-10-24T10:00:00Z")
    private val paidAt = StatusChangedAt(Instant.parse("2026-10-24T10:03:00Z"))
    private val shippedAt = StatusChangedAt(Instant.parse("2026-10-25T08:00:00Z"))

    private fun placedOrder(): Order {
        val orderPlacement = OrderPlacement(OrderStamp(OrderId("order-1"), placedAt), Checkout(customer, homeOrderAddresses))
        return Order.place(orderPlacement, Cart(customer).add(deskLamp, Quantity(1)))
    }

    @Test
    fun `a placed order should await its payment since it was placed`() {
        // Act
        val order = placedOrder()
        // Assert
        val placedStatus = OrderStatusChange(OrderStatus.AWAITING_PAYMENT, StatusChangedAt(placedAt))
        assertThat(order.history).isEqualTo(OrderStatusHistory(placedStatus))
    }

    @Test
    fun `an order should keep every status it reached, and be at the last one`() {
        // Arrange
        val paidOrder = placedOrder().paidAt(paidAt)
        val processedOrder = paidOrder.advancedAt(paidAt)
        // Act
        val shippedOrder = processedOrder.advancedAt(shippedAt)
        // Assert
        assertThat(shippedOrder.status).isEqualTo(OrderStatus.SHIPPED)
        assertThat(shippedOrder.history.asList().map { it.status }).containsExactly(
            OrderStatus.AWAITING_PAYMENT, OrderStatus.PAID, OrderStatus.PROCESSING, OrderStatus.SHIPPED
        )
        assertThat(shippedOrder.history.asList().last().changedAt).isEqualTo(shippedAt)
    }

    @Test
    fun `a history should not be empty`() {
        // Act
        val thrown = catchThrowable { OrderStatusHistory(emptyList()) }
        // Assert
        assertThat(thrown).isInstanceOf(IllegalArgumentException::class.java)
    }
}
