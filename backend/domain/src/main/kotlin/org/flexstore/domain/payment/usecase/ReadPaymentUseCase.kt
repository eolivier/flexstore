package org.flexstore.domain.payment.usecase

import org.flexstore.domain.order.OrderId
import org.flexstore.domain.payment.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.UseCaseLog

class ReadPaymentUseCase(private val paymentRepository: PaymentRepository) {

    fun getSession(orderId: OrderId): PaymentSession = UseCaseLog.execution("ReadPaymentUseCase.getSession") {
        val message = ErrorMessage("No payment started for order ${orderId.value}.")
        val notStarted = PaymentNotStartedException(message)
        paymentRepository.sessionOf(orderId) ?: throw notStarted
    }
}
