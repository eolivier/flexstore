package org.flexstore.infra.repository

import org.flexstore.domain.order.*
import org.flexstore.domain.payment.*

// A payment provider in memory: its sessions are paid at once, or confirmed later, as given
class InMemoryPaymentRepository(private val outcome: PaymentOutcome) : PaymentRepository {

    private val sessions = mutableMapOf<OrderId, PaymentSession>()

    override fun start(order: Order) {
        val checkoutPage = CheckoutPage("https://pay.example/${order.orderId.value}")
        sessions[order.orderId] = PaymentSession(checkoutPage, outcome)
    }

    override fun sessionOf(orderId: OrderId) = sessions[orderId]
}
