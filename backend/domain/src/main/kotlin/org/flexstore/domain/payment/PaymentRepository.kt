package org.flexstore.domain.payment

import org.flexstore.domain.order.OrderId

// The payment provider (Stripe, or a simulated one): the domain does not know which one
interface PaymentRepository {

    // Starts the payment of the order on the provider, prefilled for its payer, and keeps its session
    fun start(paymentRequest: PaymentRequest)

    fun sessionOf(orderId: OrderId): PaymentSession?

    fun sessionExistsFor(orderId: OrderId) = sessionOf(orderId) != null

    fun sessionIsMissingFor(orderId: OrderId) = !sessionExistsFor(orderId)
}
