package org.flexstore.domain.payment

import org.flexstore.domain.order.OrderId

// The payment provider (Stripe, or a simulated one): the domain does not know which one
interface PaymentRepository {

    // Starts the payment of the order on the provider, prefilled for its payer, and keeps its session
    fun start(paymentRequest: PaymentRequest)

    fun sessionOf(orderId: OrderId): PaymentSession?

    fun sessionExistsFor(orderId: OrderId) = sessionOf(orderId) != null

    fun sessionIsMissingFor(orderId: OrderId) = !sessionExistsFor(orderId)

    // The payment of the order can no longer complete (e.g. the payment page closes); throws
    // PaymentAlreadyCompletedException when the customer has already paid
    fun stop(orderId: OrderId)

    // Gives the payment of the paid order back to the customer, on the provider
    fun refund(orderId: OrderId)
}
