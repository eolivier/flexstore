package org.flexstore.infra.spring.adapter.rest.json

import org.flexstore.domain.sharedkernel.*
import java.math.BigDecimal
import org.flexstore.domain.settings.*
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

// orderConfirmationEmail: ENABLED or DISABLED; paymentDelayMinutes: the time to pay an order once placed;
// freeShippingThreshold: the price of the items (in euros) from which the standard shipping is free
data class JsonShopSettings(
    @JsonProperty("orderConfirmationEmail") val orderConfirmationEmail: String,
    @JsonProperty("paymentDelayMinutes") val paymentDelayMinutes: Int,
    @JsonProperty("freeShippingThreshold") val freeShippingThreshold: BigDecimal
) {
    constructor(settings: ShopSettings) : this(
        settings.orderConfirmationEmail.name,
        settings.paymentDelay.minutes,
        settings.freeShippingThreshold.price.amount.value
    )
}

// The price of the items, in euros, from which the standard shipping is free (0: always free)
data class JsonFreeShippingThreshold @JsonCreator constructor(@JsonProperty("amount") val amount: BigDecimal) {
    fun toFreeShippingThreshold() = FreeShippingThreshold(Price(Amount(amount), Currency.EUR))
}

// The time a customer has to pay an order once placed, in minutes
data class JsonPaymentDelay @JsonCreator constructor(@JsonProperty("minutes") val minutes: Int) {
    fun toPaymentDelay() = PaymentDelay(minutes)
}
