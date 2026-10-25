package org.flexstore.infra.repository

import org.flexstore.domain.order.*
import org.flexstore.domain.payment.*

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
}
