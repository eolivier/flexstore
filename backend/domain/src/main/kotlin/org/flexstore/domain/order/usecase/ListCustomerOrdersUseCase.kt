package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*
import org.flexstore.domain.sharedkernel.Page
import org.flexstore.domain.sharedkernel.PageRequest
import org.flexstore.domain.user.UserRepository
import org.flexstore.domain.user.model.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A customer reads a page of their orders
data class CustomerOrdersReading(val customer: UserId.ValidUserId, val pageRequest: PageRequest)

// A page of the orders of a customer, newest first
class ListCustomerOrdersUseCase(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
) : QueryUseCase<CustomerOrdersReading, Page<Orders>> {

    override fun getPreConditions(): PreConditions<CustomerOrdersReading> =
        PreConditions(customerCanActCondition())

    override fun getNominalQuery() = NominalQuery<CustomerOrdersReading, Page<Orders>> { reading ->
        orderRepository.findAllBy(reading.customer, reading.pageRequest)
    }

    override fun getPostConditions(): PostConditions<Page<Orders>> = PostConditions.none()

    // The customer must be an existing user whose account is not suspended, locked or disabled (read
    // from the users, not from the request)
    private fun customerCanActCondition() = PreCondition<CustomerOrdersReading> { reading ->
        userRepository.verifyCustomerCanAct(reading.customer)
    }
}
