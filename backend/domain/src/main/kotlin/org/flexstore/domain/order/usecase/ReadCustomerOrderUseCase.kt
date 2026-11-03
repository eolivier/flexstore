package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.UserRepository
import org.flexstore.domain.user.model.*
import org.ucop.domain.entity.*

// An order looked for by a customer
data class CustomerOrderLookup(val customer: UserId.ValidUserId, val orderId: OrderId)

// An order of a customer. The order of another customer is reported as not found, so that its existence is not
// revealed
class ReadCustomerOrderUseCase(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
) : QueryUseCase<CustomerOrderLookup, Order> {

    override fun getPreConditions(): PreConditions<CustomerOrderLookup> =
        PreConditions(customerCanActCondition(), orderOfTheCustomerExistsCondition())

    override fun getNominalQuery() = NominalQuery<CustomerOrderLookup, Order> { lookup ->
        orderRepository.findById(lookup.orderId)!!
    }

    override fun getPostConditions(): PostConditions<Order> = PostConditions.none()

    // The customer must be an existing user whose account is not suspended, locked or disabled (read
    // from the users, not from the request)
    private fun customerCanActCondition() = PreCondition<CustomerOrderLookup> { lookup ->
        val customerId = lookup.customer
        if (userRepository.notExists(customerId)) {
            throw UserNotFoundException(ErrorMessage("User ${customerId.value} not found."))
        }
        val status = userRepository.statusOf(customerId)
        if (status.refusesSignIn()) {
            val message = ErrorMessage("The account ${customerId.value} is $status: it cannot act.")
            throw AccountRefusedException(status, message)
        }
    }

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
