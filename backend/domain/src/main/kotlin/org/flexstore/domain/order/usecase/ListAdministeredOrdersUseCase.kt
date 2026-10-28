package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*
import org.flexstore.domain.user.UserRepository
import org.ucop.domain.entity.*

// The orders of all the customers, newest first, each one with the customer who placed it, for the
// administrators; the users are read once for all the orders
class ListAdministeredOrdersUseCase(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
) : QueryUseCase<Unit, AdministeredOrders> {

    override fun getPreConditions(): PreConditions<Unit> = PreConditions.none()

    override fun getNominalQuery() = NominalQuery<Unit, AdministeredOrders> {
        val customersById = userRepository.findAll().associate { user -> user.id to OrderCustomer.of(user) }
        val orders = orderRepository.findAll().newestFirst().asList()
        AdministeredOrders(orders.map { order ->
            val customer = customersById.getOrDefault(order.customer, OrderCustomer.unknown())
            AdministeredOrder(order, customer)
        })
    }

    override fun getPostConditions(): PostConditions<AdministeredOrders> = PostConditions.none()
}
