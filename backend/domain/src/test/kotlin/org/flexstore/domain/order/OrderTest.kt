package org.flexstore.domain.order

import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.assertj.core.api.WithAssertions
import org.flexstore.domain.settings.PaymentDelay
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import java.time.Instant

// The Order aggregate alone: no repository
class OrderTest : WithAssertions {

    private val lamp = DefinedProduct(ProductId(Identity("lamp")), Name("Desk lamp"), Price(Amount(BigDecimal(35)), Currency.EUR))
    private val mug = DefinedProduct(ProductId(Identity("mug")), Name("Mug"), Price(Amount(BigDecimal(12)), Currency.EUR))
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val placedAt = Instant.parse("2026-10-24T10:00:00Z")
    private val checkout = Checkout(customer, homeOrderAddresses)
    private val orderPlacement = OrderPlacement(OrderStamp(OrderId("order-1"), placedAt), checkout)

    @Test
    fun `should place an order with a line per cart item`() {
        // Arrange
        val cart = Cart(customer).add(lamp, Quantity(2)).add(mug, Quantity(1))
        // Act
        val order = Order.place(orderPlacement, cart)
        // Assert
        assertThat(order.lines.asList()).containsExactly(
            OrderLine.of(lamp, Quantity(2)),
            OrderLine.of(mug, Quantity(1))
        )
        assertThat(order.customer).isEqualTo(customer.userId)
        assertThat(order.status).isEqualTo(OrderStatus.AWAITING_PAYMENT)
    }

    @Test
    fun `the total price should be the sum of the lines`() {
        // Arrange
        val cart = Cart(customer).add(lamp, Quantity(2)).add(mug, Quantity(1))
        // Act
        val order = Order.place(orderPlacement, cart)
        // Assert
        assertThat(order.totalPrice().amount.value).isEqualByComparingTo("82")
    }

    @Test
    fun `the payment deadline should be the placement plus the payment delay`() {
        // Arrange
        val cart = Cart(customer).add(lamp, Quantity(1))
        val order = Order.place(orderPlacement, cart)
        // Act
        val paymentDeadline = order.paymentDeadline(PaymentDelay(10))
        // Assert
        assertThat(paymentDeadline).isEqualTo(PaymentDeadline(Instant.parse("2026-10-24T10:10:00Z")))
    }

    @ParameterizedTest(name = "the deadline is reached at {0}: {1}")
    @CsvSource("2026-10-24T10:09:59Z, false", "2026-10-24T10:10:00Z, true", "2026-10-24T10:10:01Z, true")
    fun `a payment deadline should be reached from its moment on`(instant: Instant, reached: Boolean) {
        // Arrange
        val paymentDeadline = PaymentDeadline(Instant.parse("2026-10-24T10:10:00Z"))
        // Act
        val isReached = paymentDeadline.isReachedAt(instant)
        // Assert
        assertThat(isReached).isEqualTo(reached)
    }

    @Test
    fun `should not place an order from an empty cart`() {
        // Arrange
        val cart = Cart(customer)
        // Act
        val thrown = catchThrowable { Order.place(orderPlacement, cart) }
        // Assert
        assertThat(thrown).isInstanceOf(EmptyCartException::class.java)
    }

    @Test
    fun `orders should be sorted newest first`() {
        // Arrange
        val cart = Cart(customer).add(lamp, Quantity(1))
        val olderOrderPlacement = OrderPlacement(OrderStamp(OrderId("older"), placedAt), checkout)
        val newerOrderPlacement = OrderPlacement(OrderStamp(OrderId("newer"), placedAt.plusSeconds(60)), checkout)
        val older = Order.place(olderOrderPlacement, cart)
        val newer = Order.place(newerOrderPlacement, cart)
        // Act
        val sorted = Orders(older, newer).newestFirst()
        // Assert
        assertThat(sorted.asList()).containsExactly(newer, older)
    }
}
