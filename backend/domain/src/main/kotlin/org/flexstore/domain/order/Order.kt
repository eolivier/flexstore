package org.flexstore.domain.order

import org.flexstore.domain.cart.model.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.NominalException
import org.ucop.domain.NonEmptyString
import java.math.BigDecimal
import java.time.Instant

/**
 * Order aggregate: placed by a customer from their cart.
 * - an order is placed from a cart that has items
 * - its lines keep the name and the price of the products when the order is placed
 * - a new order is being processed
 */
data class Order(
    val orderId: OrderId,
    val customer: UserId.ValidUserId,
    val lines: OrderLines,
    val status: OrderStatus,
    val placedAt: Instant
) {
    fun totalPrice() = lines.totalPrice()

    fun belongsTo(customer: UserId.ValidUserId) = this.customer == customer

    companion object {
        fun place(placement: OrderPlacement, cart: Cart): Order {
            if (cart.isEmpty()) {
                throw EmptyCartException(NonEmptyString("An order cannot be placed from an empty cart."))
            }
            return Order(placement.orderId, placement.customer.userId, OrderLines.from(cart), OrderStatus.PROCESSING, placement.placedAt)
        }
    }
}

// An order to place: its id (chosen by the caller, which can then read the order), its customer, its date
data class OrderPlacement(val orderId: OrderId, val customer: CartOwner.Customer, val placedAt: Instant)

data class OrderId(val value: String) {
    companion object {
        fun nextId() = OrderId(Identity.nextId().value)
    }
}

enum class OrderStatus { PROCESSING, SHIPPED, DELIVERED }

data class OrderLine(val productId: ProductId, val productName: Name, val unitPrice: Price, val quantity: Quantity) {
    fun linePrice() = unitPrice.multiply(quantity)
}

data class OrderLines(private val lines: List<OrderLine>) {

    constructor(vararg lines: OrderLine) : this(lines.toList())

    fun totalPrice(): Price {
        val currency = lines.firstOrNull()?.unitPrice?.currency ?: Currency.EUR
        val total = lines.fold(BigDecimal.ZERO) { sum, line -> sum.add(line.linePrice().amount.value) }
        return Price(Amount(total), currency)
    }

    // Read-only view for the adapters (JSON, persistence)
    fun asList(): List<OrderLine> = lines.toList()

    companion object {
        fun from(cart: Cart) = OrderLines(cart.items.fold(emptyList()) { lines, product, quantity ->
            lines + OrderLine(product.productId, product.name, product.price, quantity)
        })
    }
}

data class Orders(private val orders: List<Order>) {

    constructor(vararg orders: Order) : this(orders.toList())

    fun newestFirst() = Orders(orders.sortedByDescending { it.placedAt })

    fun asList(): List<Order> = orders.toList()
}

data class EmptyCartException(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
data class OrderNotFoundException(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
data class OrderPlacementFailed(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
