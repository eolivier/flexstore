package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.UserRepository
import org.ucop.domain.entity.*

// Any order, with the customer who placed it, for the administrators; the customer is unknown when their
// account cannot be read any more
class ReadAdministeredOrderUseCase(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
) : QueryUseCase<OrderId, AdministeredOrder> {

    override fun getPreConditions(): PreConditions<OrderId> = PreConditions(orderExistsCondition())

    override fun getNominalQuery() = NominalQuery<OrderId, AdministeredOrder> { orderId ->
        val order = orderRepository.findById(orderId)!!
        AdministeredOrder(order, customerOf(order))
    }

    override fun getPostConditions(): PostConditions<AdministeredOrder> = PostConditions.none()

    private fun orderExistsCondition() = PreCondition<OrderId> { orderId ->
        if (orderRepository.orderIsMissing(orderId)) {
            throw OrderNotFoundException(ErrorMessage("Order ${orderId.value} not found."))
        }
    }

    private fun customerOf(order: Order): OrderCustomer {
        if (userRepository.exists(order.customer)) {
            return OrderCustomer.of(userRepository.findById(order.customer))
        }
        return OrderCustomer.unknown()
    }
}
