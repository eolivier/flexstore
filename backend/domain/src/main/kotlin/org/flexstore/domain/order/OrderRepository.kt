package org.flexstore.domain.order

import org.flexstore.domain.user.model.*

interface OrderRepository {

    fun save(order: Order): Order

    fun findById(orderId: OrderId): Order?

    fun orderExists(orderId: OrderId): Boolean

    fun orderIsMissing(orderId: OrderId): Boolean = !orderExists(orderId)

    fun findAllBy(customer: UserId.ValidUserId): Orders

    // The orders of all the customers
    fun findAll(): Orders
}
