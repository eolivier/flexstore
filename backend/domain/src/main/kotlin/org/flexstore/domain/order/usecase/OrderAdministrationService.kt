package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*

// Domain service: the administrators follow the orders of all the customers, with the customer of each one, and
// move them along their life cycle. An order moved to its next status is read back, all through the use cases
class OrderAdministrationService(
    private val listAdministeredOrdersUseCase: ListAdministeredOrdersUseCase,
    private val readAdministeredOrderUseCase: ReadAdministeredOrderUseCase,
    private val advanceOrderStatusUseCase: AdvanceOrderStatusUseCase
) {
    fun getAllOrders(): AdministeredOrders = listAdministeredOrdersUseCase.unfold(Unit)

    fun getOrder(orderId: OrderId): AdministeredOrder = readAdministeredOrderUseCase.unfold(orderId)

    fun advance(orderStatusAdvance: OrderStatusAdvance): AdministeredOrder {
        advanceOrderStatusUseCase.unfold(orderStatusAdvance)
        return readAdministeredOrderUseCase.unfold(orderStatusAdvance.orderId)
    }
}
