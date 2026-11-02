package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.UserRepository
import org.ucop.domain.entity.*

// A customer reads one of their orders with their own name and email, to download its invoice or its credit
// note. The order of another customer is reported as not found, so that its existence is not revealed
class ReadCustomerOrderWithCustomerUseCase(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
) : QueryUseCase<CustomerOrderLookup, AdministeredOrder> {

    override fun getPreConditions(): PreConditions<CustomerOrderLookup> =
        PreConditions(orderOfTheCustomerExistsCondition())

    override fun getNominalQuery() = NominalQuery<CustomerOrderLookup, AdministeredOrder> { lookup ->
        val order = orderRepository.findById(lookup.orderId)!!
        AdministeredOrder(order, OrderCustomer.of(userRepository.findById(lookup.customer)))
    }

    override fun getPostConditions(): PostConditions<AdministeredOrder> = PostConditions.none()

    // Unknown, or an order of another customer
    private fun orderIsMissingFor(lookup: CustomerOrderLookup) =
        orderRepository.orderIsMissing(lookup.orderId) ||
            orderRepository.findById(lookup.orderId)!!.doesNotBelongTo(lookup.customer)

    private fun orderOfTheCustomerExistsCondition() = PreCondition<CustomerOrderLookup> { lookup ->
        if (orderIsMissingFor(lookup)) {
            throw OrderNotFoundException(ErrorMessage("Order ${lookup.orderId.value} not found."))
        }
    }
}
