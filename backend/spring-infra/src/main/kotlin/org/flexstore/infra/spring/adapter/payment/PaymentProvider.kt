package org.flexstore.infra.spring.adapter.payment

import org.flexstore.domain.order.Order
import org.flexstore.domain.payment.CheckoutPage
import org.flexstore.domain.payment.PaymentOutcome

// The checkout of an order on the payment provider: its reference there, the page where the customer pays,
// and whether the payment is already confirmed
data class ProviderCheckout(val reference: String, val checkoutPage: CheckoutPage, val outcome: PaymentOutcome)

// The payment provider used by the PaymentRepositoryAdapter: Stripe, or a simulated one without a Stripe key
interface PaymentProvider {
    fun checkout(order: Order): ProviderCheckout
}
