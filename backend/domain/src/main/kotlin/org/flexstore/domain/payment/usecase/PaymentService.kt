package org.flexstore.domain.payment.usecase

import org.flexstore.domain.order.*
import org.flexstore.domain.order.usecase.*
import org.flexstore.domain.payment.*

// Domain service: a customer pays an order on the payment provider, which confirms the payment (at once, or
// later through its notification). Once paid, the order is confirmed to the customer by email.
// All through the use cases
class PaymentService(
    private val paymentUseCases: PaymentUseCases,
    private val paidOrderUseCases: PaidOrderUseCases
) {
    fun pay(orderPayment: OrderPayment): PaymentSession {
        paymentUseCases.startPaymentUseCase.unfold(orderPayment)
        val paymentSession = paymentUseCases.readPaymentUseCase.getSession(orderPayment.orderId)
        if (paymentSession.isPaid()) confirm(orderPayment.orderId)
        return paymentSession
    }

    // The provider may notify the same payment several times: an order already paid stays as it is
    fun confirm(orderId: OrderId) {
        val order = paidOrderUseCases.readOrdersUseCase.getAnyOrder(orderId)
        if (order.isPaid()) return
        paymentUseCases.confirmPaymentUseCase.unfold(orderId)
        val paidOrder = paidOrderUseCases.readOrdersUseCase.getAnyOrder(orderId)
        paidOrderUseCases.sendOrderConfirmationEmailUseCase.unfold(paidOrder)
    }
}

// The use cases of the payment
data class PaymentUseCases(
    val startPaymentUseCase: StartPaymentUseCase,
    val confirmPaymentUseCase: ConfirmPaymentUseCase,
    val readPaymentUseCase: ReadPaymentUseCase
)

// The use cases of the order that the payment needs: reading it, confirming it to the customer
data class PaidOrderUseCases(
    val readOrdersUseCase: ReadOrdersUseCase,
    val sendOrderConfirmationEmailUseCase: SendOrderConfirmationEmailUseCase
)
