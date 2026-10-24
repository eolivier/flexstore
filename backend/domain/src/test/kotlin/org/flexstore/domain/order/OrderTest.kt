package org.flexstore.domain.order

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.entity.*
import org.flexstore.domain.valueobject.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

// The Order aggregate alone: no repository
class OrderTest : WithAssertions {

    private val lamp = DefinedProduct(ProductId(Identity("lamp")), Name("Desk lamp"), Price(Amount(BigDecimal(35)), Currency.EUR))
    private val mug = DefinedProduct(ProductId(Identity("mug")), Name("Mug"), Price(Amount(BigDecimal(12)), Currency.EUR))
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val placedAt = Instant.parse("2026-10-24T10:00:00Z")

    @Test
    fun `should place an order with a line per cart item`() {
        // Arrange
        val cart = Cart(customer).add(lamp, Quantity(2)).add(mug, Quantity(1))
        // Act
        val order = Order.place(PlaceOrderRequest(OrderId("order-1"), customer, placedAt), cart)
        // Assert
        assertThat(order.lines.asList()).containsExactly(
            OrderLine(lamp.productId, Name("Desk lamp"), lamp.price, Quantity(2)),
            OrderLine(mug.productId, Name("Mug"), mug.price, Quantity(1))
        )
        assertThat(order.customer).isEqualTo(customer.userId)
        assertThat(order.status).isEqualTo(OrderStatus.PROCESSING)
    }

    @Test
    fun `the total price should be the sum of the lines`() {
        // Arrange
        val cart = Cart(customer).add(lamp, Quantity(2)).add(mug, Quantity(1))
        // Act
        val order = Order.place(PlaceOrderRequest(OrderId("order-1"), customer, placedAt), cart)
        // Assert
        assertThat(order.totalPrice().amount.value).isEqualByComparingTo("82")
    }

    @Test
    fun `should not place an order from an empty cart`() {
        // Arrange
        val cart = Cart(customer)
        // Act
        val thrown = catchThrowable { Order.place(PlaceOrderRequest(OrderId("order-1"), customer, placedAt), cart) }
        // Assert
        assertThat(thrown).isInstanceOf(EmptyCartException::class.java)
    }

    @Test
    fun `orders should be sorted newest first`() {
        // Arrange
        val cart = Cart(customer).add(lamp, Quantity(1))
        val older = Order.place(PlaceOrderRequest(OrderId("older"), customer, placedAt), cart)
        val newer = Order.place(PlaceOrderRequest(OrderId("newer"), customer, placedAt.plusSeconds(60)), cart)
        // Act
        val sorted = Orders(listOf(older, newer)).newestFirst()
        // Assert
        assertThat(sorted.asList()).containsExactly(newer, older)
    }
}
