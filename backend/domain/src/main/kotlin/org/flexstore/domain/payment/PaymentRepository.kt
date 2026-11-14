package org.flexstore.domain.payment

import org.flexstore.domain.order.model.OrderId
import org.ucop.domain.ErrorMessage

// The payment provider (Stripe, or a simulated one): the domain does not know which one
interface PaymentRepository {

    // Starts the payment of the order on the provider, prefilled for its payer, and keeps its session
    fun start(paymentRequest: PaymentRequest)

    fun sessionOf(orderId: OrderId): PaymentSession?

    // The session, which a precondition has checked to exist
    fun getSessionOf(orderId: OrderId): PaymentSession =
        sessionOf(orderId) ?: throw PaymentNotStartedException(ErrorMessage("No payment started for order $orderId."))

    fun sessionExistsFor(orderId: OrderId) = sessionOf(orderId) != null

    fun sessionIsMissingFor(orderId: OrderId) = !sessionExistsFor(orderId)

    // The customer paid the order on the provider, whose notification may be late or lost (so the order may
    // still await its payment here); false when no payment started
    fun isCompleted(orderId: OrderId): Boolean

    // The payment of the order can no longer complete (e.g. the payment page closes); throws
    // PaymentAlreadyCompletedException when the customer has already paid
    fun stop(orderId: OrderId)

    // Gives the payment of the paid order back to the customer, on the provider
    fun refund(orderId: OrderId)
}
