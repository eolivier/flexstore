package org.flexstore.domain.order.service

import org.flexstore.domain.order.usecase.*
import org.flexstore.domain.order.*
import org.flexstore.domain.sharedkernel.Page
import org.flexstore.domain.sharedkernel.PageRequest
import org.flexstore.domain.user.model.UserId

// Domain service: the administrators follow the orders of all the customers, with the customer of each one,
// move them along their life cycle, and correct the tracking of a parcel. An order moved is read back, then
// its customer is told when it is shipped or delivered, all through the use cases. Refunding an order is the
// OrderRefundService
class OrderAdministrationService(
    private val listAdministeredOrdersUseCase: ListAdministeredOrdersUseCase,
    private val readAdministeredOrderUseCase: ReadAdministeredOrderUseCase,
    private val advanceOrderStatusUseCase: AdvanceOrderStatusUseCase,
    private val correctOrderTrackingUseCase: CorrectOrderTrackingUseCase,
    private val sendOrderStatusEmailUseCase: SendOrderStatusEmailUseCase
) {
    fun getAllOrders(reader: UserId, pageRequest: PageRequest): Page<AdministeredOrders> =
        listAdministeredOrdersUseCase.unfold(AdministeredOrdersReading(reader, pageRequest))

    fun getOrder(reader: UserId, orderId: OrderId): AdministeredOrder =
        readAdministeredOrderUseCase.unfold(AdministeredOrderReading(reader, orderId))

    fun advance(orderStatusAdvance: OrderStatusAdvance): AdministeredOrder {
        advanceOrderStatusUseCase.unfold(orderStatusAdvance)
        val advancedOrder = getOrder(orderStatusAdvance.editor, orderStatusAdvance.orderId)
        sendOrderStatusEmailUseCase.unfold(advancedOrder.order)
        return advancedOrder
    }

    // The customer is not told again: the correction is read on their order
    fun correctTracking(orderTrackingCorrection: OrderTrackingCorrection): AdministeredOrder {
        correctOrderTrackingUseCase.unfold(orderTrackingCorrection)
        return getOrder(orderTrackingCorrection.editor, orderTrackingCorrection.orderId)
    }
}
