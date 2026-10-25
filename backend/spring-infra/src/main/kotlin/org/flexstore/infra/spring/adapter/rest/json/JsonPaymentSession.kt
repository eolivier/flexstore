package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.payment.PaymentSession

// The page where the customer pays the order; PAID when the payment is already confirmed (simulated)
data class JsonPaymentSession(
    @JsonProperty("checkoutUrl") val checkoutUrl: String,
    @JsonProperty("outcome") val outcome: String
) {
    companion object {
        fun of(paymentSession: PaymentSession) =
            JsonPaymentSession(paymentSession.checkoutPage.url, paymentSession.outcome.name)
    }
}
