package org.flexstore.domain.order.model

import java.time.Instant
import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.settings.PaymentDelay
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*

// The time a customer has to pay, checked at a given moment
data class PaymentTimeLimit(val paymentDelay: PaymentDelay, val checkedAt: Instant) {

    // The order still awaits its payment, and its payment deadline is reached
    fun isExceededFor(order: Order) =
        order.isAwaitingPayment() && order.paymentDeadline(paymentDelay).isReachedAt(checkedAt)

    fun isNotExceededFor(order: Order) = !isExceededFor(order)
}

// The moment an order must be paid by: its placement plus the payment delay
data class PaymentDeadline(val value: Instant) {

    fun isReachedAt(instant: Instant) = instant >= value
}
