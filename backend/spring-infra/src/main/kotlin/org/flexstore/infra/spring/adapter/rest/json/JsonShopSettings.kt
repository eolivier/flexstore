package org.flexstore.infra.spring.adapter.rest.json

import org.flexstore.domain.settings.*
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

// orderConfirmationEmail: ENABLED or DISABLED; paymentDelayMinutes: the time to pay an order once placed
data class JsonShopSettings(
    @JsonProperty("orderConfirmationEmail") val orderConfirmationEmail: String,
    @JsonProperty("paymentDelayMinutes") val paymentDelayMinutes: Int
) {
    constructor(settings: ShopSettings) :
        this(settings.orderConfirmationEmail.name, settings.paymentDelay.minutes)
}

// The time a customer has to pay an order once placed, in minutes
data class JsonPaymentDelay @JsonCreator constructor(@JsonProperty("minutes") val minutes: Int) {
    fun toPaymentDelay() = PaymentDelay(minutes)
}
