package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.*
import org.flexstore.domain.sharedkernel.Page
import org.flexstore.domain.user.repository.UserRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator reads a page of the orders of all the customers, newest first, each one with the customer
// who placed it; the users are read once for all the orders of the page
class ListAdministeredOrdersUseCase(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
) : QueryUseCase<AdministeredOrdersReading, Page<AdministeredOrders>> {

    override fun getPreConditions(): PreConditions<AdministeredOrdersReading> =
        PreConditions(readerIsAdministratorCondition())

    override fun getNominalQuery() =
        NominalQuery<AdministeredOrdersReading, Page<AdministeredOrders>> { reading ->
            val customersById = userRepository.findAll().associate { user -> user.id to OrderCustomer.of(user) }
            val ordersPage = orderRepository.findAll(reading.pageRequest)
            val administeredOrders = ordersPage.content.asList().map { order ->
                val customer = customersById.getOrDefault(order.customer, OrderCustomer.unknown())
                AdministeredOrder(order, customer)
            }
            Page(AdministeredOrders(administeredOrders), ordersPage.total)
        }

    override fun getPostConditions(): PostConditions<Page<AdministeredOrders>> = PostConditions.none()

    // The reader must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun readerIsAdministratorCondition() = PreCondition<AdministeredOrdersReading> { request ->
        if (userRepository.isNotAdministrator(request.reader)) {
            throw OrderAdministrationForbiddenException(ErrorMessage("Only an administrator can read the orders of all the customers."))
        }
    }
}
