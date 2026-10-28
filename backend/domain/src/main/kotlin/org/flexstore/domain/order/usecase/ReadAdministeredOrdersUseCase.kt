package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.UserRepository
import org.ucop.domain.UseCaseLog

// The orders of all the customers, each one with the customer who placed it, for the administrators
class ReadAdministeredOrdersUseCase(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
) {

    // Newest first; the users are read once for all the orders
    fun getAllOrders(): AdministeredOrders = UseCaseLog.execution("ReadAdministeredOrdersUseCase.getAllOrders") {
        val customersById = userRepository.findAll().associate { user -> user.id to OrderCustomer.of(user) }
        val orders = orderRepository.findAll().newestFirst().asList()
        AdministeredOrders(orders.map { order ->
            val customer = customersById.getOrDefault(order.customer, OrderCustomer.unknown())
            AdministeredOrder(order, customer)
        })
    }

    fun getOrder(orderId: OrderId): AdministeredOrder = UseCaseLog.execution(GET_ORDER) {
        val notFound = OrderNotFoundException(ErrorMessage("Order ${orderId.value} not found."))
        val order = orderRepository.findById(orderId) ?: throw notFound
        AdministeredOrder(order, customerOf(order))
    }

    private fun customerOf(order: Order): OrderCustomer {
        if (userRepository.exists(order.customer)) {
            return OrderCustomer.of(userRepository.findById(order.customer))
        }
        return OrderCustomer.unknown()
    }
}

private const val GET_ORDER = "ReadAdministeredOrdersUseCase.getOrder"
