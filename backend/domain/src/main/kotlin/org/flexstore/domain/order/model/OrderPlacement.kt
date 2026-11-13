package org.flexstore.domain.order.model

import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*

// An order to place: its stamp (id and date) and its checkout (customer and addresses)
data class OrderPlacement(val stamp: OrderStamp, val checkout: Checkout) {

    val orderId get() = stamp.orderId

    val placedAt get() = stamp.placedAt

    val customer get() = checkout.customer

    val addresses get() = checkout.addresses
}
