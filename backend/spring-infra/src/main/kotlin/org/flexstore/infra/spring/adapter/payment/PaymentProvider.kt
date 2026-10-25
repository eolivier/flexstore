package org.flexstore.infra.spring.adapter.payment

import org.flexstore.domain.payment.CheckoutPage
import org.flexstore.domain.payment.PaymentOutcome
import org.flexstore.domain.payment.PaymentRequest

// The checkout of an order on the payment provider: its reference there, the page where the customer pays,
// and whether the payment is already confirmed
data class ProviderCheckout(val reference: String, val checkoutPage: CheckoutPage, val outcome: PaymentOutcome)

// The payment provider used by the PaymentRepositoryAdapter: Stripe, or a simulated one without a Stripe key
interface PaymentProvider {
    fun checkout(paymentRequest: PaymentRequest): ProviderCheckout
}
