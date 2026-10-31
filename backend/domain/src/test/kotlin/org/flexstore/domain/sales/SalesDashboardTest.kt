package org.flexstore.domain.sales

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.cart.Cart
import org.flexstore.domain.cart.CartOwner
import org.flexstore.domain.order.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.UserId
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

class SalesDashboardTest : WithAssertions {

    private val lamp = DefinedProduct(ProductId(Identity("lamp")), Name("Desk lamp"), euros(35))
    private val chair = DefinedProduct(ProductId(Identity("chair")), Name("Chair"), euros(100))
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val lastDay = SalesDay(LocalDate.parse("2026-10-31"))
    private val week = SalesPeriod(lastDay, DayCount(7))

    private fun euros(amount: Int) = Price(Amount(BigDecimal(amount)), Currency.EUR)

    private fun order(placedAt: String, status: OrderStatus, product: DefinedProduct, quantity: Int): Order {
        val cart = Cart(customer).add(product, Quantity(quantity))
        val stamp = OrderStamp(OrderId.nextId(), Instant.parse(placedAt))
        val placement = OrderPlacement(stamp, Checkout(customer, homeOrderAddresses))
        return Order.place(placement, cart).withStatus(status)
    }

    @Test
    fun `the revenue and the orders of the period should only count the paid orders`() {
        // Arrange
        val paid = order("2026-10-30T10:00:00Z", OrderStatus.PAID, chair, 1)
        val delivered = order("2026-10-31T09:00:00Z", OrderStatus.DELIVERED, chair, 2)
        val orders = Orders(
            paid, delivered,
            order("2026-10-30T10:00:00Z", OrderStatus.AWAITING_PAYMENT, chair, 1),
            order("2026-10-30T10:00:00Z", OrderStatus.CANCELLED, chair, 1),
            order("2026-10-30T10:00:00Z", OrderStatus.REFUNDED, chair, 1)
        )
        // Act
        val dashboard = SalesDashboard.of(orders, week)
        // Assert
        assertThat(dashboard.orderCount).isEqualTo(OrderCount(2))
        assertThat(dashboard.revenue).isEqualTo(Orders(paid, delivered).revenue())
    }

    @Test
    fun `the orders placed out of the period should not count`() {
        // Arrange
        val before = order("2026-10-24T10:00:00Z", OrderStatus.PAID, chair, 1)
        val inside = order("2026-10-25T10:00:00Z", OrderStatus.PAID, chair, 1)
        // Act
        val dashboard = SalesDashboard.of(Orders(before, inside), week)
        // Assert
        assertThat(dashboard.orderCount).isEqualTo(OrderCount(1))
    }

    @Test
    fun `the orders per day should list every day of the period, the days without order included`() {
        // Arrange
        val orders = Orders(
            order("2026-10-29T10:00:00Z", OrderStatus.PAID, lamp, 1),
            order("2026-10-29T15:00:00Z", OrderStatus.SHIPPED, lamp, 1)
        )
        // Act
        val days = SalesDashboard.of(orders, week).dailySales.asList()
        // Assert
        assertThat(days).hasSize(7)
        assertThat(days.first().day).isEqualTo(SalesDay(LocalDate.parse("2026-10-25")))
        assertThat(days.last().day).isEqualTo(lastDay)
        assertThat(days.single { it.orderCount == OrderCount(2) }.day)
            .isEqualTo(SalesDay(LocalDate.parse("2026-10-29")))
        assertThat(days.count { it.orderCount.isZero() }).isEqualTo(6)
    }

    @Test
    fun `an order placed just after midnight in Paris should count on the day in Paris`() {
        // Arrange: 23:30 UTC on the 30th is 00:30 on the 31st in Paris (UTC+1 since the 25th of October)
        val orders = Orders(order("2026-10-30T23:30:00Z", OrderStatus.PAID, lamp, 1))
        // Act
        val days = SalesDashboard.of(orders, week).dailySales.asList()
        // Assert
        assertThat(days.last().orderCount).isEqualTo(OrderCount(1))
    }

    @Test
    fun `the best sellers should be the products with the most units sold, five at most`() {
        // Arrange
        val orders = Orders(
            order("2026-10-30T10:00:00Z", OrderStatus.PAID, lamp, 3),
            order("2026-10-31T10:00:00Z", OrderStatus.PAID, lamp, 2),
            order("2026-10-31T11:00:00Z", OrderStatus.PAID, chair, 4)
        )
        // Act
        val bestSellers = SalesDashboard.of(orders, week).bestSellers.asList()
        // Assert
        assertThat(bestSellers.map { it.product.productName.value }).containsExactly("Desk lamp", "Chair")
        assertThat(bestSellers.first().unitsSold).isEqualTo(Quantity(5))
        assertThat(bestSellers.first().revenue).isEqualTo(euros(175))
    }

    @Test
    fun `a period without sale should have no revenue and no best seller`() {
        // Act
        val dashboard = SalesDashboard.of(Orders(), week)
        // Assert
        assertThat(dashboard.revenue).isEqualTo(euros(0))
        assertThat(dashboard.bestSellers.asList()).isEmpty()
    }

    @ParameterizedTest(name = "{0} days: {1}")
    @CsvSource("7, true", "30, true", "90, true", "1, false", "0, false", "365, false")
    fun `only 7, 30 or 90 days should be allowed`(days: Int, allowed: Boolean) {
        // Act
        val dayCount = DayCount(days)
        // Assert
        assertThat(dayCount.isAllowed()).isEqualTo(allowed)
    }
}
