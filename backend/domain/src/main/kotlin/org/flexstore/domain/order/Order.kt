package org.flexstore.domain.order

import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.settings.PaymentDelay
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
 * - a new order awaits its payment, which reserves its units; unpaid in time, it is cancelled
 * - it is always delivered to a delivery address and billed to a billing address (often the same),
 *   both addresses of the customer
 */
data class Order(val detail: OrderDetail, val status: OrderStatus) {

    val orderId get() = detail.placement.orderId

    val customer get() = detail.placement.customer.userId

    val placedAt get() = detail.placement.placedAt

    val addresses get() = detail.placement.addresses

    val lines get() = detail.lines

    fun totalPrice() = lines.totalPrice()

    // The number shown to the customer: the beginning of the order id
    fun number() = orderId.value.take(ORDER_NUMBER_LENGTH).uppercase()

    fun belongsTo(customer: UserId.ValidUserId) = this.customer == customer

    // The order at the next status of its life cycle
    fun advanced() = copy(status = status.next())

    // Not paid in time: its units go back to the stock
    fun cancelled() = copy(status = OrderStatus.CANCELLED)

    // Paid, even after it was cancelled for lack of payment (the customer paid just before)
    fun paid() = copy(status = OrderStatus.PAID)

    fun isAwaitingPayment() = status == OrderStatus.AWAITING_PAYMENT

    fun isCancelled() = status == OrderStatus.CANCELLED

    fun isPaid() = status.isPaid()

    companion object {
        fun place(orderPlacement: OrderPlacement, cart: Cart): Order {
            if (cart.isEmpty()) {
                throw EmptyCartException(ErrorMessage("An order cannot be placed from an empty cart."))
            }
            if (orderPlacement.addresses.isIncomplete()) {
                val message = ErrorMessage("An order needs a complete delivery address and billing address.")
                throw IncompleteOrderAddressException(message)
            }
            val orderLines = OrderLines.from(cart)
            val orderDetail = OrderDetail(orderPlacement, orderLines)
            return Order(orderDetail, OrderStatus.AWAITING_PAYMENT)
        }
    }
}

// What was ordered, fixed once the order is placed: how it was placed and its lines (the status changes
// afterwards)
data class OrderDetail(val placement: OrderPlacement, val lines: OrderLines)

// An order to place: its stamp (id and date) and its checkout (customer and addresses)
data class OrderPlacement(val stamp: OrderStamp, val checkout: Checkout) {

    val orderId get() = stamp.orderId

    val placedAt get() = stamp.placedAt

    val customer get() = checkout.customer

    val addresses get() = checkout.addresses
}

// The id of a new order (chosen by the caller, which can then read the order) and the date it is placed
data class OrderStamp(val orderId: OrderId, val placedAt: Instant)

// What the customer chooses when checking out: who orders, where the order is delivered and billed
data class Checkout(val customer: CartOwner.Customer, val addresses: OrderAddresses)

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

// The life cycle of an order: awaiting its payment, paid, being processed, then shipped, then delivered (the
// last status). The payment moves it from AWAITING_PAYMENT to PAID, the administrators along the next statuses.
// An order not paid in time is CANCELLED, which has no next status
enum class OrderStatus {
    AWAITING_PAYMENT, PAID, PROCESSING, SHIPPED, DELIVERED, CANCELLED;

    fun hasNext() = this != DELIVERED && this != CANCELLED

    fun isPaid() = this in listOf(PAID, PROCESSING, SHIPPED, DELIVERED)

    fun isLast() = this == DELIVERED

    fun isMovedByAnAdministrator() = this != AWAITING_PAYMENT && hasNext()

    fun isNotMovedByAnAdministrator() = !isMovedByAnAdministrator()

    fun next(): OrderStatus = when (this) {
        AWAITING_PAYMENT -> PAID
        PAID -> PROCESSING
        PROCESSING -> SHIPPED
        SHIPPED -> DELIVERED
        DELIVERED -> throw OrderStatusCannotAdvanceException(ErrorMessage("A delivered order has no next status"))
        CANCELLED -> throw OrderStatusCannotAdvanceException(ErrorMessage("A cancelled order has no next status"))
    }
}

// An order to move to the next status of its life cycle, from the status it has when the administrator sees it
data class OrderStatusAdvance(val orderId: OrderId, val currentStatus: OrderStatus)

// A product as ordered: its id and its name when the order was placed
data class OrderedProduct(val productId: ProductId, val productName: Name)

// A product as ordered, with its unit price when the order was placed
data class PricedProduct(val product: OrderedProduct, val unitPrice: Price)

// A line of an order: a product, at its price when the order was placed, and its quantity
data class OrderLine(val product: PricedProduct, val quantity: Quantity) {

    val productId get() = product.product.productId

    val productName get() = product.product.productName

    val unitPrice get() = product.unitPrice

    fun linePrice() = unitPrice.multiply(quantity)

    companion object {
        // The line of a product of the catalog, with its name and its price at this time
        fun of(definedProduct: DefinedProduct, quantity: Quantity): OrderLine {
            val orderedProduct = OrderedProduct(definedProduct.productId, definedProduct.name)
            return OrderLine(PricedProduct(orderedProduct, definedProduct.price), quantity)
        }
    }
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
            lines + OrderLine.of(product, quantity)
        })
    }
}

data class Orders(private val orders: List<Order>) {

    constructor(vararg orders: Order) : this(orders.toList())

    fun newestFirst() = Orders(orders.sortedByDescending { it.placedAt })

    // The orders still awaiting their payment once the time limit is exceeded
    fun latePaymentsAt(timeLimit: PaymentTimeLimit) = Orders(orders.filter { timeLimit.isExceededFor(it) })

    fun asList(): List<Order> = orders.toList()
}

// The time a customer has to pay, checked at a given moment
data class PaymentTimeLimit(val paymentDelay: PaymentDelay, val checkedAt: Instant) {

    // The order still awaits its payment, and was placed at least the payment delay ago
    fun isExceededFor(order: Order) =
        order.isAwaitingPayment() && !checkedAt.isBefore(order.placedAt + paymentDelay.asDuration())
}

// An order to cancel because it was not paid in time
data class UnpaidOrderCancellation(val orderId: OrderId, val timeLimit: PaymentTimeLimit)

data class OrderNotAwaitingPaymentException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
data class PaymentTimeLimitNotExceededException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
data class OrderCancelledException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class OrderCancellationFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class EmptyCartException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class IncompleteOrderAddressException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
data class OrderRefusedException(val status: AccountStatus, override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
data class OrderStatusCannotAdvanceException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
data class OrderStatusChangedException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class OrderStatusAdvanceFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class OrderNotFoundException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class OrderPlacementFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
