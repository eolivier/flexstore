package org.flexstore.infra.spring.adapter.payment

import org.flexstore.domain.payment.CheckoutPage
import org.flexstore.domain.payment.PaymentOutcome
import org.flexstore.domain.payment.PaymentRequest
import org.flexstore.domain.sharedkernel.Identity
import org.slf4j.LoggerFactory

// Without a Stripe key (locally, in the tests): every payment is confirmed at once,
// and the customer goes straight to the confirmation page of the order
class SimulatedPaymentProvider(private val frontUrl: String) : PaymentProvider {

    private val logger = LoggerFactory.getLogger(SimulatedPaymentProvider::class.java)

    override fun checkout(paymentRequest: PaymentRequest): ProviderCheckout {
        val order = paymentRequest.order
        logger.info("Simulated payment of order {} (no Stripe key)", order.number())
        val orderPage = "${frontUrl.trimEnd('/')}/orders/${order.orderId.value}"
        val confirmationPage = CheckoutPage("$orderPage/confirmation")
        return ProviderCheckout("simulated-${Identity.nextId().value}", confirmationPage, PaymentOutcome.PAID)
    }
}
