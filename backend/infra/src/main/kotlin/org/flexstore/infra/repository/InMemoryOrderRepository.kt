package org.flexstore.infra.repository

import org.flexstore.domain.order.*
import org.flexstore.domain.sharedkernel.Page
import org.flexstore.domain.sharedkernel.PageRequest
import org.flexstore.domain.sharedkernel.TotalCount
import org.flexstore.domain.user.model.*

class InMemoryOrderRepository : OrderRepository {

    private val orders = mutableMapOf<OrderId, Order>()

    override fun save(order: Order): Order {
        orders[order.orderId] = order
        return order
    }

    override fun findById(orderId: OrderId): Order? = orders[orderId]

    override fun orderExists(orderId: OrderId): Boolean = orders.containsKey(orderId)

    override fun findAllBy(customer: UserId.ValidUserId) =
        Orders(orders.values.filter { it.customer == customer })

    override fun findAll() = Orders(orders.values.toList())

    override fun findAllBy(customer: UserId.ValidUserId, pageRequest: PageRequest) =
        pageOf(findAllBy(customer), pageRequest)

    override fun findAll(pageRequest: PageRequest) = pageOf(findAll(), pageRequest)

    private fun pageOf(allOrders: Orders, pageRequest: PageRequest): Page<Orders> {
        val newestFirst = allOrders.newestFirst().asList()
        val pageSize = pageRequest.size.value
        val pageOrders = newestFirst.drop(pageRequest.number.value * pageSize).take(pageSize)
        return Page(Orders(pageOrders), TotalCount(newestFirst.size.toLong()))
    }
}
