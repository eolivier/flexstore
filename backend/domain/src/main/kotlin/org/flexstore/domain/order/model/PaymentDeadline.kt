package org.flexstore.domain.order.model

import java.time.Instant
import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*

// The moment an order must be paid by: its placement plus the payment delay
data class PaymentDeadline(val value: Instant) {

    fun isReachedAt(instant: Instant) = instant >= value
}
