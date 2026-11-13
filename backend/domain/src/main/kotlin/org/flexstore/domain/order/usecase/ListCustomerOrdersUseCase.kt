package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.*
import org.flexstore.domain.sharedkernel.Page
import org.flexstore.domain.sharedkernel.PageRequest
import org.flexstore.domain.actor.customerId
import org.flexstore.domain.actor.verifyCanAct
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A customer, resolved before the use case runs, reads a page of their orders
data class CustomerOrdersReading(
    val customer: Actor,
    val pageRequest: PageRequest
)

// A page of the orders of a customer, newest first
class ListCustomerOrdersUseCase(
    private val orderRepository: OrderRepository
) : QueryUseCase<CustomerOrdersReading, Page<Orders>> {

    override fun getPreConditions(): PreConditions<CustomerOrdersReading> =
        PreConditions(customerCanActCondition())

    override fun getNominalQuery() = NominalQuery<CustomerOrdersReading, Page<Orders>> { reading ->
        orderRepository.findAllBy(reading.customer.customerId(), reading.pageRequest)
    }

    override fun getPostConditions(): PostConditions<Page<Orders>> = PostConditions.none()

    // The customer, resolved before the use case runs, must be a customer or an admin who can act
    private fun customerCanActCondition() = PreCondition<CustomerOrdersReading> { reading ->
        reading.customer.verifyCanAct()
    }
}
