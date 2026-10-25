package org.flexstore.domain.order

import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.NominalException
import org.flexstore.domain.sharedkernel.ErrorMessage
import java.math.BigDecimal
import java.time.Instant

// Number of characters of the order id shown to the customer as the order number
private const val ORDER_NUMBER_LENGTH = 8

/**
 * Order aggregate: placed by a customer from their cart.
 * - an order is placed from a cart that has items
 * - its lines keep the name and the price of the products when the order is placed
 * - a new order is being processed
 * - it is always delivered to a delivery address and billed to a billing address (often the same), both addresses of the customer
 */
data class Order(
    val orderId: OrderId,
    val customer: UserId.ValidUserId,
    val lines: OrderLines,
    val status: OrderStatus,
    val placedAt: Instant,
    val addresses: OrderAddresses
) {
    fun totalPrice() = lines.totalPrice()

    // The number shown to the customer: the beginning of the order id
    fun number() = orderId.value.take(ORDER_NUMBER_LENGTH).uppercase()

    fun belongsTo(customer: UserId.ValidUserId) = this.customer == customer

    companion object {
        fun place(orderPlacement: OrderPlacement, cart: Cart): Order {
            if (cart.isEmpty()) {
                throw EmptyCartException(ErrorMessage("An order cannot be placed from an empty cart."))
            }
            if (orderPlacement.addresses.isIncomplete()) {
                throw IncompleteOrderAddressException(ErrorMessage("An order needs a complete delivery address and billing address."))
            }
            val orderLines = OrderLines.from(cart)
            return Order(
                orderPlacement.orderId, orderPlacement.customer.userId, orderLines, OrderStatus.PROCESSING, orderPlacement.placedAt,
                orderPlacement.addresses
            )
        }
    }
}

// An order to place: its id (chosen by the caller, which can then read the order), its customer, its date, its addresses
data class OrderPlacement(val orderId: OrderId, val customer: CartOwner.Customer, val placedAt: Instant, val addresses: OrderAddresses)

// Where the order is delivered and to which address it is billed (often the same)
data class OrderAddresses(val delivery: PostalAddress, val billing: PostalAddress) {

    fun isComplete() = delivery.isComplete() && billing.isComplete()

    fun isIncomplete() = !isComplete()

    fun isBilledToTheDeliveryAddress() = delivery == billing

    companion object {
        fun sameFor(address: PostalAddress) = OrderAddresses(address, address)
    }
}

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

data class EmptyCartException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class IncompleteOrderAddressException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class OrderRefusedException(val status: AccountStatus, override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class OrderNotFoundException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class OrderPlacementFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
