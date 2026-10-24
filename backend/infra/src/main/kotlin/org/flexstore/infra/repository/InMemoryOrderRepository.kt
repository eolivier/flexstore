package org.flexstore.infra.repository

import org.flexstore.domain.order.Order
import org.flexstore.domain.order.OrderId
import org.flexstore.domain.order.Orders
import org.flexstore.domain.entity.UserId
import org.flexstore.domain.order.OrderRepository

class InMemoryOrderRepository : OrderRepository {

    private val orders = mutableMapOf<OrderId, Order>()

    override fun save(order: Order): Order {
        orders[order.orderId] = order
        return order
    }

    override fun findById(orderId: OrderId): Order? = orders[orderId]

    override fun orderExists(orderId: OrderId): Boolean = orders.containsKey(orderId)

    override fun findAllBy(customer: UserId.ValidUserId) = Orders(orders.values.filter { it.customer == customer })
}
