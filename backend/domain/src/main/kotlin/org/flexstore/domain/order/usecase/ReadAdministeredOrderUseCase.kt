package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*
import org.flexstore.domain.user.UserRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An order, with the customer who placed it, read by an administrator (any order) or by its customer (to
// download its invoice or its credit note); the customer is unknown when their account cannot be read any more
class ReadAdministeredOrderUseCase(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
) : QueryUseCase<AdministeredOrderReading, AdministeredOrder> {

    override fun getPreConditions(): PreConditions<AdministeredOrderReading> =
        PreConditions(orderIsReadableByTheReaderCondition())

    override fun getNominalQuery() = NominalQuery<AdministeredOrderReading, AdministeredOrder> { (_, orderId) ->
        val order = orderRepository.findById(orderId)!!
        AdministeredOrder(order, customerOf(order))
    }

    override fun getPostConditions(): PostConditions<AdministeredOrder> = PostConditions.none()

    // Unknown, or, for a reader who is not an administrator (read from the users, not from the request), an
    // order of another customer
    private fun orderIsMissingFor(reading: AdministeredOrderReading): Boolean {
        val (reader, orderId) = reading
        if (orderRepository.orderIsMissing(orderId)) return true
        val order = orderRepository.findById(orderId)!!
        return userRepository.isNotAdministrator(reader) && order.doesNotBelongTo(reader)
    }

    // An administrator reads any order, a customer their own ones; the order of another customer is reported as
    // not found, so that its existence is not revealed
    private fun orderIsReadableByTheReaderCondition() = PreCondition<AdministeredOrderReading> { reading ->
        if (orderIsMissingFor(reading)) {
            throw OrderNotFoundException(ErrorMessage("Order ${reading.orderId.value} not found."))
        }
    }

    private fun customerOf(order: Order): OrderCustomer {
        if (userRepository.exists(order.customer)) {
            return OrderCustomer.of(userRepository.findById(order.customer))
        }
        return OrderCustomer.unknown()
    }
}
