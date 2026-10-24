package org.flexstore.domain.order.usecase

import org.ucop.domain.UseCaseLog
import org.flexstore.domain.order.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.sharedkernel.ErrorMessage

class ReadOrdersUseCase(private val orderRepository: OrderRepository) {

    fun getOrders(customer: UserId.ValidUserId): Orders =
        UseCaseLog.execution("ReadOrdersUseCase.getOrders") { orderRepository.findAllBy(customer).newestFirst() }

    // The order of another customer is reported as not found, so that its existence is not revealed
    fun getOrder(customer: UserId.ValidUserId, orderId: OrderId): Order = UseCaseLog.execution("ReadOrdersUseCase.getOrder") {
        orderRepository.findById(orderId)?.takeIf { it.belongsTo(customer) }
            ?: throw OrderNotFoundException(ErrorMessage("Order ${orderId.value} not found."))
    }
}
