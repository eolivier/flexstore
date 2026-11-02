package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.UserRepository
import org.ucop.domain.entity.*

// An administrator reads any order, with the customer who placed it; the customer is unknown when their account
// cannot be read any more
class ReadAdministeredOrderUseCase(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
) : QueryUseCase<AdministeredOrderReading, AdministeredOrder> {

    override fun getPreConditions(): PreConditions<AdministeredOrderReading> =
        PreConditions(readerIsAdministratorCondition(), orderExistsCondition())

    override fun getNominalQuery() = NominalQuery<AdministeredOrderReading, AdministeredOrder> { (_, orderId) ->
        val order = orderRepository.findById(orderId)!!
        AdministeredOrder(order, customerOf(order))
    }

    override fun getPostConditions(): PostConditions<AdministeredOrder> = PostConditions.none()

    // The reader must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun readerIsAdministratorCondition() = PreCondition<AdministeredOrderReading> { request ->
        if (userRepository.isNotAdministrator(request.reader)) {
            throw OrderAdministrationForbiddenException(ErrorMessage("Only an administrator can read any order."))
        }
    }

    private fun orderExistsCondition() = PreCondition<AdministeredOrderReading> { (_, orderId) ->
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
