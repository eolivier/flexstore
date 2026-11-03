package org.flexstore.domain.order.service

import org.flexstore.domain.order.usecase.*
import org.flexstore.domain.order.*
import org.flexstore.domain.user.model.UserId

// Domain service: the administrators follow the orders of all the customers, with the customer of each one, and
// move them along their life cycle. An order moved is read back, then its customer is told when it is shipped or
// delivered, all through the use cases. Refunding an order is the OrderRefundService
class OrderAdministrationService(
    private val listAdministeredOrdersUseCase: ListAdministeredOrdersUseCase,
    private val readAdministeredOrderUseCase: ReadAdministeredOrderUseCase,
    private val advanceOrderStatusUseCase: AdvanceOrderStatusUseCase,
    private val sendOrderStatusEmailUseCase: SendOrderStatusEmailUseCase
) {
    fun getAllOrders(reader: UserId): AdministeredOrders =
        listAdministeredOrdersUseCase.unfold(AdministeredOrdersReading(reader))

    fun getOrder(reader: UserId, orderId: OrderId): AdministeredOrder =
        readAdministeredOrderUseCase.unfold(AdministeredOrderReading(reader, orderId))

    fun advance(orderStatusAdvance: OrderStatusAdvance): AdministeredOrder {
        advanceOrderStatusUseCase.unfold(orderStatusAdvance)
        val advancedOrder = getOrder(orderStatusAdvance.editor, orderStatusAdvance.orderId)
        sendOrderStatusEmailUseCase.unfold(advancedOrder.order)
        return advancedOrder
    }
}
