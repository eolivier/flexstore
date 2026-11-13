package org.flexstore.domain.order.model

import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*

data class OrderId(val value: String) {
    companion object {
        fun nextId() = OrderId(Identity.nextId().value)
    }
}
