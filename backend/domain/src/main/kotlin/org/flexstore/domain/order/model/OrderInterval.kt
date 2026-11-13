package org.flexstore.domain.order.model

import java.time.Instant
import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*

// From a moment included to another excluded
data class OrderInterval(val start: Instant, val end: Instant) {
    fun contains(instant: Instant) = instant >= start && instant < end
}
