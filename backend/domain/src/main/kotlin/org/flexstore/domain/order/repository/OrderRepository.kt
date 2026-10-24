package org.flexstore.domain.order.repository

import org.flexstore.domain.order.model.*
import org.flexstore.domain.user.model.*

interface OrderRepository {

    fun save(order: Order): Order

    fun findById(orderId: OrderId): Order?

    fun orderExists(orderId: OrderId): Boolean

    fun orderIsMissing(orderId: OrderId): Boolean = !orderExists(orderId)

    fun findAllBy(customer: UserId.ValidUserId): Orders
}
