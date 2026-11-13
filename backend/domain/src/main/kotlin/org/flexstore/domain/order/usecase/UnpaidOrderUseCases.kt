package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.model.*
import org.ucop.domain.entity.Actor

// The use cases of the unpaid orders: every order is listed, and the late ones cancelled
class UnpaidOrderUseCases(
    private val listAllOrdersUseCase: ListAllOrdersUseCase,
    private val cancelUnpaidOrderUseCase: CancelUnpaidOrderUseCase
) {
    fun allOrders() = listAllOrdersUseCase.unfold(Unit)

    fun cancel(canceller: Actor, order: Order, timeLimit: PaymentTimeLimit) =
        cancelUnpaidOrderUseCase.unfold(UnpaidOrderCancellation(canceller, order.orderId, timeLimit))
}
