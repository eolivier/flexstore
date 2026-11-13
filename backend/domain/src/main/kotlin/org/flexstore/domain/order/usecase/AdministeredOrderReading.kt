package org.flexstore.domain.order.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.order.model.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.entity.Actor

// A user who reads an order with its customer: an administrator, or the customer of the order
data class AdministeredOrderReading(val reader: Actor, val orderId: OrderId)
