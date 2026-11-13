package org.flexstore.domain.order.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.order.model.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.entity.Actor

// An order to cancel because it was not paid in time
data class UnpaidOrderCancellation(val actor: Actor, val orderId: OrderId, val timeLimit: PaymentTimeLimit)
