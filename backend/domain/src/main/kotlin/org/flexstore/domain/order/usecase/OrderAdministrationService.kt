package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*

// Domain service: the administrators follow the orders of all the customers, with the customer of each one, and
// move them along their life cycle, or cancel and refund them before they are shipped. An order moved or
// refunded is read back, then its customer is told when it is shipped, delivered or refunded, all through the
// use cases
class OrderAdministrationService(
    private val listAdministeredOrdersUseCase: ListAdministeredOrdersUseCase,
    private val readAdministeredOrderUseCase: ReadAdministeredOrderUseCase,
    private val advanceOrderStatusUseCase: AdvanceOrderStatusUseCase,
    private val refundOrderUseCase: RefundOrderUseCase,
    private val sendOrderStatusEmailUseCase: SendOrderStatusEmailUseCase
) {
    fun getAllOrders(): AdministeredOrders = listAdministeredOrdersUseCase.unfold(Unit)

    fun getOrder(orderId: OrderId): AdministeredOrder = readAdministeredOrderUseCase.unfold(orderId)

    fun advance(orderStatusAdvance: OrderStatusAdvance): AdministeredOrder {
        advanceOrderStatusUseCase.unfold(orderStatusAdvance)
        val advancedOrder = readAdministeredOrderUseCase.unfold(orderStatusAdvance.orderId)
        sendOrderStatusEmailUseCase.unfold(advancedOrder.order)
        return advancedOrder
    }

    fun refund(orderRefund: OrderRefund): AdministeredOrder {
        refundOrderUseCase.unfold(orderRefund)
        val refundedOrder = readAdministeredOrderUseCase.unfold(orderRefund.orderId)
        sendOrderStatusEmailUseCase.unfold(refundedOrder.order)
        return refundedOrder
    }
}
