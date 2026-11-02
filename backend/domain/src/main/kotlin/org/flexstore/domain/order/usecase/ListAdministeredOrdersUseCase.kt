package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*
import org.flexstore.domain.user.UserRepository
import org.ucop.domain.entity.*
import org.flexstore.domain.sharedkernel.ErrorMessage

// An administrator reads the orders of all the customers, newest first, each one with the customer who placed
// it; the users are read once for all the orders
class ListAdministeredOrdersUseCase(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
) : QueryUseCase<AdministeredOrdersReading, AdministeredOrders> {

    override fun getPreConditions(): PreConditions<AdministeredOrdersReading> =
        PreConditions(readerIsAdministratorCondition())

    override fun getNominalQuery() = NominalQuery<AdministeredOrdersReading, AdministeredOrders> {
        val customersById = userRepository.findAll().associate { user -> user.id to OrderCustomer.of(user) }
        val orders = orderRepository.findAll().newestFirst().asList()
        AdministeredOrders(orders.map { order ->
            val customer = customersById.getOrDefault(order.customer, OrderCustomer.unknown())
            AdministeredOrder(order, customer)
        })
    }

    override fun getPostConditions(): PostConditions<AdministeredOrders> = PostConditions.none()

    // The reader must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun readerIsAdministratorCondition() = PreCondition<AdministeredOrdersReading> { request ->
        if (userRepository.isNotAdministrator(request.reader)) {
            throw OrderAdministrationForbiddenException(ErrorMessage("Only an administrator can read the orders of all the customers."))
        }
    }
}
