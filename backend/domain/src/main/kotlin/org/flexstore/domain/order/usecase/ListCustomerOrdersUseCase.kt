package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.UserRepository
import org.flexstore.domain.user.model.*
import org.ucop.domain.entity.*

// The orders of a customer, newest first
class ListCustomerOrdersUseCase(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
) : QueryUseCase<UserId.ValidUserId, Orders> {

    override fun getPreConditions(): PreConditions<UserId.ValidUserId> =
        PreConditions(customerCanActCondition())

    override fun getNominalQuery() = NominalQuery<UserId.ValidUserId, Orders> { customer ->
        orderRepository.findAllBy(customer).newestFirst()
    }

    override fun getPostConditions(): PostConditions<Orders> = PostConditions.none()

    // The customer must be an existing user whose account is not suspended, locked or disabled (read
    // from the users, not from the request)
    private fun customerCanActCondition() = PreCondition<UserId.ValidUserId> { customer ->
        if (userRepository.notExists(customer)) {
            throw UserNotFoundException(ErrorMessage("User ${customer.value} not found."))
        }
        val status = userRepository.statusOf(customer)
        if (status.refusesSignIn()) {
            val message = ErrorMessage("The account ${customer.value} is $status: it cannot act.")
            throw AccountRefusedException(status, message)
        }
    }
}
