package org.flexstore.domain.order.model

import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*

// What was ordered, fixed once the order is placed: how it was placed, and what it costs (its lines and its
// shipping); the status changes afterwards
data class OrderDetail(val placement: OrderPlacement, val bill: OrderBill)
