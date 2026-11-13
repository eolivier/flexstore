package org.flexstore.domain.order.model

import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.settings.FreeShippingThreshold
import org.flexstore.domain.settings.PaymentDelay
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.ErrorMessage

// Number of characters of the order id shown to the customer as the order number
private const val ORDER_NUMBER_LENGTH = 8

/**
 * Order aggregate: placed by a customer from their cart.
 * - an order is placed from a cart that has items
 * - its lines keep the name and the price of the products when the order is placed
 * - a new order awaits its payment, which reserves its units; unpaid in time, it is cancelled
 * - it is always delivered to a delivery address and billed to a billing address (often the same),
 *   both addresses of the customer
 * - it keeps the statuses it went through, with the moment it reached each one: its status is the last one
 * - it keeps the version of the terms of sale its customer accepted to place it
 * - once shipped, it may carry the carrier and the tracking number of its parcel
 */
data class Order(
    val detail: OrderDetail,
    val history: OrderStatusHistory,
    val tracking: OrderTracking = OrderTracking.None
) {

    val status get() = history.currentStatus()

    val orderId get() = detail.placement.orderId

    val customer get() = detail.placement.customer.userId

    val placedAt get() = detail.placement.placedAt

    val addresses get() = detail.placement.addresses

    val termsOfSale get() = detail.placement.checkout.termsOfSale

    val lines get() = detail.bill.lines

    val shipping get() = detail.bill.shipping

    val discount get() = detail.bill.discount

    fun discountAmount() = detail.bill.discountAmount()

    // The price of the items, without the shipping
    fun itemsPrice() = detail.bill.itemsPrice()

    fun totalPrice() = detail.bill.totalPrice()

    // The number shown to the customer: the beginning of the order id
    fun number() = orderId.value.take(ORDER_NUMBER_LENGTH).uppercase()

    fun belongsTo(customer: UserId) = this.customer == customer

    fun doesNotBelongTo(customer: UserId) = !belongsTo(customer)

    // The order at the next status of its life cycle
    fun advancedAt(changedAt: StatusChangedAt) = movedTo(status.next(), changedAt)

    // Not paid in time: its units go back to the stock
    fun cancelledAt(changedAt: StatusChangedAt) = movedTo(OrderStatus.CANCELLED, changedAt)

    // Paid, even after it was cancelled for lack of payment (the customer paid just before)
    fun paidAt(changedAt: StatusChangedAt) = movedTo(OrderStatus.PAID, changedAt)

    // Cancelled by the shop after its payment, which is given back to the customer
    fun refundedAt(changedAt: StatusChangedAt) = movedTo(OrderStatus.REFUNDED, changedAt)

    // The parcel of the order is followed with this carrier and this number; nothing given changes nothing
    fun withTracking(tracking: OrderTracking) = when (tracking) {
        is OrderTracking.Given -> copy(tracking = tracking)
        OrderTracking.None -> this
    }

    private fun movedTo(status: OrderStatus, changedAt: StatusChangedAt) =
        copy(history = history.with(OrderStatusChange(status, changedAt)))

    fun isRefunded() = status == OrderStatus.REFUNDED

    fun isAwaitingPayment() = status == OrderStatus.AWAITING_PAYMENT

    fun isNotAwaitingPayment() = !isAwaitingPayment()

    fun isCancelled() = status == OrderStatus.CANCELLED

    fun isPaid() = status.isPaid()

    fun isNotPaid() = !isPaid()

    // The order has a line of the product
    fun includes(productId: ProductId) = lines.includes(productId)

    // The moment the order must be paid by, or it is cancelled
    fun paymentDeadline(paymentDelay: PaymentDelay) = PaymentDeadline(placedAt + paymentDelay.asDuration())

    companion object {
        // An order of a shop whose settings are the default ones
        fun place(orderPlacement: OrderPlacement, cart: Cart) =
            place(orderPlacement, OrderContent(cart, FreeShippingThreshold.byDefault()))

        fun place(orderPlacement: OrderPlacement, orderContent: OrderContent): Order {
            val cart = orderContent.cart
            if (cart.isEmpty()) {
                throw EmptyCartException(ErrorMessage("An order cannot be placed from an empty cart."))
            }
            if (orderPlacement.addresses.isIncomplete()) {
                val message = ErrorMessage("An order needs a complete delivery address and billing address.")
                throw IncompleteOrderAddressException(message)
            }
            val orderLines = OrderLines.from(cart)
            val shippingMethod = orderPlacement.checkout.delivery.method
            val freeShippingThreshold = orderContent.freeShippingThreshold
            val shippingCost = shippingMethod.costFor(orderLines.totalPrice(), freeShippingThreshold)
            val orderDiscount = orderContent.discountOn(orderLines.totalPrice())
            val orderAdjustments = OrderAdjustments(OrderShipping(shippingMethod, shippingCost), orderDiscount)
            val orderBill = OrderBill(orderLines, orderAdjustments)
            val orderDetail = OrderDetail(orderPlacement, orderBill)
            val placedAt = StatusChangedAt(orderPlacement.placedAt)
            val placedStatus = OrderStatusChange(OrderStatus.AWAITING_PAYMENT, placedAt)
            return Order(orderDetail, OrderStatusHistory(placedStatus))
        }
    }
}
