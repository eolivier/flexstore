package org.flexstore.domain.order.model

import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.ErrorMessage

// The life cycle of an order: awaiting its payment, paid, being processed, then shipped, then delivered (the
// last status). The payment moves it from AWAITING_PAYMENT to PAID, the administrators along the next statuses.
// An order not paid in time is CANCELLED; a paid order cancelled by an administrator before it is shipped is
// REFUNDED. Neither has a next status
enum class OrderStatus {
    AWAITING_PAYMENT, PAID, PROCESSING, SHIPPED, DELIVERED, CANCELLED, REFUNDED;

    fun hasNext() = this != DELIVERED && this != CANCELLED && this != REFUNDED

    fun isPaid() = this in listOf(PAID, PROCESSING, SHIPPED, DELIVERED)

    fun isMovedByAnAdministrator() = this != AWAITING_PAYMENT && hasNext()

    fun isNotMovedByAnAdministrator() = !isMovedByAnAdministrator()

    // The customer is told by email when their order is shipped, then delivered, or refunded
    fun isAnnouncedToTheCustomer() = this == SHIPPED || this == DELIVERED || this == REFUNDED

    // The next status of a processed order is the one that gives its carrier and its tracking number
    fun isShippedNext() = this == PROCESSING

    // Shipped, or delivered: an administrator may still correct the tracking of its parcel
    fun allowsTrackingCorrection() = this == SHIPPED || this == DELIVERED

    fun refusesTrackingCorrection() = !allowsTrackingCorrection()

    // Paid, and not shipped yet: an administrator may cancel it and refund it
    fun isRefundable() = this == PAID || this == PROCESSING

    fun isNotRefundable() = !isRefundable()

    fun next(): OrderStatus = when (this) {
        AWAITING_PAYMENT -> PAID
        PAID -> PROCESSING
        PROCESSING -> SHIPPED
        SHIPPED -> DELIVERED
        DELIVERED -> throw noNextStatus("delivered")
        CANCELLED -> throw noNextStatus("cancelled")
        REFUNDED -> throw noNextStatus("refunded")
    }

    private fun noNextStatus(state: String): OrderStatusCannotAdvanceException {
        val message = ErrorMessage("A $state order has no next status")
        return OrderStatusCannotAdvanceException(message)
    }
}
