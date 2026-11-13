package org.flexstore.domain.order.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.order.model.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.entity.Actor

// An order that an administrator cancels and refunds, from the status it has when the administrator sees it
data class OrderRefund(val editor: Actor, val orderId: OrderId, val currentStatus: OrderStatus)
