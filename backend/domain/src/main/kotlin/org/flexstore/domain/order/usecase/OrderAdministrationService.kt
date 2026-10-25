package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*

// Domain service: the administrators follow the orders of all the customers and move them along their life
// cycle. An order moved to its next status is read back, all through the use cases
class OrderAdministrationService(
    private val readOrdersUseCase: ReadOrdersUseCase,
    private val advanceOrderStatusUseCase: AdvanceOrderStatusUseCase
) {
    fun getAllOrders(): Orders = readOrdersUseCase.getAllOrders()

    fun advance(orderStatusAdvance: OrderStatusAdvance): Order {
        advanceOrderStatusUseCase.unfold(orderStatusAdvance)
        return readOrdersUseCase.getAnyOrder(orderStatusAdvance.orderId)
    }
}
