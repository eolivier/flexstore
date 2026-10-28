package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*

// Domain service: the administrators follow the orders of all the customers, with the customer of each one, and
// move them along their life cycle. An order moved to its next status is read back, then its customer is told
// when it is shipped or delivered, all through the use cases
class OrderAdministrationService(
    private val readAdministeredOrdersUseCase: ReadAdministeredOrdersUseCase,
    private val advanceOrderStatusUseCase: AdvanceOrderStatusUseCase,
    private val sendOrderStatusEmailUseCase: SendOrderStatusEmailUseCase
) {
    fun getAllOrders(): AdministeredOrders = readAdministeredOrdersUseCase.getAllOrders()

    fun getOrder(orderId: OrderId): AdministeredOrder = readAdministeredOrdersUseCase.getOrder(orderId)

    fun advance(orderStatusAdvance: OrderStatusAdvance): AdministeredOrder {
        advanceOrderStatusUseCase.unfold(orderStatusAdvance)
        val advancedOrder = readAdministeredOrdersUseCase.getOrder(orderStatusAdvance.orderId)
        sendOrderStatusEmailUseCase.unfold(advancedOrder.order)
        return advancedOrder
    }
}
