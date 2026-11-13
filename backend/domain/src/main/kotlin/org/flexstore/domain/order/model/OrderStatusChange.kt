package org.flexstore.domain.order.model

import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*

// An order reached a status at a given moment
data class OrderStatusChange(val status: OrderStatus, val changedAt: StatusChangedAt)
