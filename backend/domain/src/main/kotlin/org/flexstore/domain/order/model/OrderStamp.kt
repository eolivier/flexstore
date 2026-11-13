package org.flexstore.domain.order.model

import java.time.Instant
import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*

// The id of a new order (chosen by the caller, which can then read the order) and the date it is placed
data class OrderStamp(val orderId: OrderId, val placedAt: Instant)
