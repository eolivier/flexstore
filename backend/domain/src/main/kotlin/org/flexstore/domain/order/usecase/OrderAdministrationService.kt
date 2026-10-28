package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*

// Domain service: the administrators follow the orders of all the customers, with the customer of each one, and
// move them along their life cycle. An order moved to its next status is read back, all through the use cases
class OrderAdministrationService(
    private val readAdministeredOrdersUseCase: ReadAdministeredOrdersUseCase,
    private val advanceOrderStatusUseCase: AdvanceOrderStatusUseCase
) {
    fun getAllOrders(): AdministeredOrders = readAdministeredOrdersUseCase.getAllOrders()

    fun getOrder(orderId: OrderId): AdministeredOrder = readAdministeredOrdersUseCase.getOrder(orderId)

    fun advance(orderStatusAdvance: OrderStatusAdvance): AdministeredOrder {
        advanceOrderStatusUseCase.unfold(orderStatusAdvance)
        return readAdministeredOrdersUseCase.getOrder(orderStatusAdvance.orderId)
    }
}
