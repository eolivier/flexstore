package org.flexstore.domain.order

import org.flexstore.domain.entity.UserId
import org.ucop.domain.NonEmptyString

class ReadOrdersUseCase(private val orderRepository: OrderRepository) {

    fun getOrders(customer: UserId.ValidUserId): Orders = orderRepository.findAllBy(customer).newestFirst()

    // The order of another customer is reported as not found, so that its existence is not revealed
    fun getOrder(customer: UserId.ValidUserId, orderId: OrderId): Order =
        orderRepository.findById(orderId)?.takeIf { it.belongsTo(customer) }
            ?: throw OrderNotFoundException(NonEmptyString("Order ${orderId.value} not found."))
}
