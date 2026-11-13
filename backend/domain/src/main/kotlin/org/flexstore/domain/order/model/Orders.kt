package org.flexstore.domain.order.model

import java.math.BigDecimal
import java.time.Instant
import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*

data class Orders(private val orders: List<Order>) {

    constructor(vararg orders: Order) : this(orders.toList())

    fun newestFirst() = Orders(orders.sortedByDescending { it.placedAt })

    // One of the orders is paid and has a line of the product: the customer bought it
    fun hasPaidFor(productId: ProductId) = orders.any { order -> order.isPaid() && order.includes(productId) }

    // None of the orders is paid with a line of the product: the customer did not buy it
    fun hasNotPaidFor(productId: ProductId) = !hasPaidFor(productId)

    // The orders that were paid, and not cancelled or refunded since: the sales of the shop
    fun paid() = Orders(orders.filter { order -> order.isPaid() })

    fun placedDuring(interval: OrderInterval) =
        Orders(orders.filter { order -> interval.contains(order.placedAt) })

    fun count() = OrderCount(orders.size)

    // What the orders cost their customers: the items, minus the discounts, plus the shipping
    fun revenue(): Price {
        val currency = orders.firstOrNull()?.totalPrice()?.currency ?: Currency.EUR
        val total = orders.fold(BigDecimal.ZERO) { sum, order -> sum.add(order.totalPrice().amount.value) }
        return Price(Amount(total), currency)
    }

    // The lines of all the orders
    fun lines() = OrderLines(orders.flatMap { order -> order.lines.asList() })

    // The orders still awaiting their payment once the time limit is exceeded
    fun latePaymentsAt(timeLimit: PaymentTimeLimit) = Orders(orders.filter { timeLimit.isExceededFor(it) })

    fun asList(): List<Order> = orders.toList()
}

data class OrderCount(val value: Int) {
    fun isZero() = value == 0
}

// From a moment included to another excluded
data class OrderInterval(val start: Instant, val end: Instant) {
    fun contains(instant: Instant) = instant >= start && instant < end
}
