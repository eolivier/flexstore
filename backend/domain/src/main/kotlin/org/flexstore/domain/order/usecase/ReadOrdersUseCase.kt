package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.model.*
import org.flexstore.domain.order.repository.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.NonEmptyString

class ReadOrdersUseCase(private val orderRepository: OrderRepository) {

    fun getOrders(customer: UserId.ValidUserId): Orders = orderRepository.findAllBy(customer).newestFirst()

    // The order of another customer is reported as not found, so that its existence is not revealed
    fun getOrder(customer: UserId.ValidUserId, orderId: OrderId): Order =
        orderRepository.findById(orderId)?.takeIf { it.belongsTo(customer) }
            ?: throw OrderNotFoundException(NonEmptyString("Order ${orderId.value} not found."))
}
