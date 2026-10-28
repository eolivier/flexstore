package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*

// Any order, whoever its customer (the payment provider confirms it)
class ReadAnyOrderUseCase(private val orderRepository: OrderRepository) : QueryUseCase<OrderId, Order> {

    override fun getPreConditions(): PreConditions<OrderId> = PreConditions(orderExistsCondition())

    override fun getNominalQuery() = NominalQuery<OrderId, Order> { orderId ->
        orderRepository.findById(orderId)!!
    }

    override fun getPostConditions(): PostConditions<Order> = PostConditions.none()

    private fun orderExistsCondition() = PreCondition<OrderId> { orderId ->
        if (orderRepository.orderIsMissing(orderId)) {
            throw OrderNotFoundException(ErrorMessage("Order ${orderId.value} not found."))
        }
    }
}
