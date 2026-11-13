package org.flexstore.domain.order.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.order.model.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.entity.Actor

// An order to move to the next status of its life cycle, from the status it has when the administrator sees
// it; moving a processed order to shipped may give the tracking of its parcel
data class OrderStatusAdvance(
    val editor: Actor,
    val orderId: OrderId,
    val currentStatus: OrderStatus,
    val tracking: OrderTracking = OrderTracking.None
)
