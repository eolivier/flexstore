package org.flexstore.infra.repository

import org.flexstore.domain.order.*
import org.flexstore.domain.payment.*
import org.flexstore.domain.sharedkernel.ErrorMessage

// A payment provider in memory: its sessions are paid at once, or confirmed later, as given
class InMemoryPaymentRepository(private val outcome: PaymentOutcome) : PaymentRepository {

    private val sessions = mutableMapOf<OrderId, PaymentSession>()

    private val payers = mutableMapOf<OrderId, Payer>()

    override fun start(paymentRequest: PaymentRequest) {
        val orderId = paymentRequest.order.orderId
        sessions[orderId] = PaymentSession(CheckoutPage("https://pay.example/${orderId.value}"), outcome)
        payers[orderId] = paymentRequest.payer
    }

    // Who the payment page of the order was prefilled for
    fun payerOf(orderId: OrderId) = payers[orderId]

    override fun sessionOf(orderId: OrderId) = sessions[orderId]

    private val stoppedPayments = mutableSetOf<OrderId>()

    private val completedPayments = mutableSetOf<OrderId>()

    // The customer paid on the provider, which has not confirmed it yet
    fun completePaymentOf(orderId: OrderId) {
        completedPayments.add(orderId)
    }

    override fun stop(orderId: OrderId) {
        if (orderId in completedPayments) {
            throw PaymentAlreadyCompletedException(ErrorMessage("Order ${orderId.value} is already paid."))
        }
        stoppedPayments.add(orderId)
    }

    fun isStopped(orderId: OrderId) = orderId in stoppedPayments
}
