package org.flexstore.domain.order

import org.flexstore.domain.sharedkernel.Page
import org.flexstore.domain.sharedkernel.PageRequest
import org.flexstore.domain.user.model.*

interface OrderRepository {

    fun save(order: Order): Order

    fun findById(orderId: OrderId): Order?

    fun orderExists(orderId: OrderId): Boolean

    fun orderIsMissing(orderId: OrderId): Boolean = !orderExists(orderId)

    fun findAllBy(customer: UserId.ValidUserId): Orders

    // A page of the orders of a customer, newest first, with the number of all their orders
    fun findAllBy(customer: UserId.ValidUserId, pageRequest: PageRequest): Page<Orders>

    // The orders of all the customers
    fun findAll(): Orders

    // A page of the orders of all the customers, newest first, with the number of all the orders
    fun findAll(pageRequest: PageRequest): Page<Orders>
}
