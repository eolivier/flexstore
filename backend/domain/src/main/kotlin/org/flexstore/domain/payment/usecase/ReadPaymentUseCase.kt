package org.flexstore.domain.payment.usecase

import org.flexstore.domain.order.model.OrderId
import org.flexstore.domain.payment.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// The payment started for an order
class ReadPaymentUseCase(
    private val paymentRepository: PaymentRepository
) : QueryUseCase<OrderId, PaymentSession> {

    override fun getPreConditions(): PreConditions<OrderId> = PreConditions(paymentStartedCondition())

    override fun getNominalQuery() = NominalQuery<OrderId, PaymentSession> { orderId ->
        paymentRepository.getSessionOf(orderId)
    }

    override fun getPostConditions(): PostConditions<PaymentSession> = PostConditions.none()

    private fun paymentStartedCondition() = PreCondition<OrderId> { orderId ->
        if (paymentRepository.sessionIsMissingFor(orderId)) {
            throw PaymentNotStartedException(ErrorMessage("No payment started for order ${orderId}."))
        }
    }
}
