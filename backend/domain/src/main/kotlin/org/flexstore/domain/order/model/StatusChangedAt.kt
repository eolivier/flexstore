package org.flexstore.domain.order.model

import java.time.Instant
import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*

// The moment an order reached a status
data class StatusChangedAt(val value: Instant) {
    companion object {
        fun now() = StatusChangedAt(InstantProvider.now())
    }
}
