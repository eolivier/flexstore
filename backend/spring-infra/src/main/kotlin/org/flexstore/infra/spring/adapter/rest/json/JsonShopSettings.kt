package org.flexstore.infra.spring.adapter.rest.json

import org.flexstore.domain.sharedkernel.*
import java.math.BigDecimal
import org.flexstore.domain.settings.*
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

// orderConfirmationEmail: ENABLED or DISABLED; paymentDelayMinutes: the time to pay an order once placed;
// freeShippingThreshold: the price of the items (in euros) from which the standard shipping is free;
// abandonedCartReminder: ENABLED or DISABLED, the email that reminds a customer of the items left in their cart;
// abandonedCartDelayHours: the time a cart is left before its customer is reminded;
// sellerSetup: ENABLED once the setup assistant of the seller is finished (read only)
data class JsonShopSettings(
    @JsonProperty("orderConfirmationEmail") val orderConfirmationEmail: String,
    @JsonProperty("paymentDelayMinutes") val paymentDelayMinutes: Int,
    @JsonProperty("freeShippingThreshold") val freeShippingThreshold: BigDecimal,
    @JsonProperty("sellerSetup") val sellerSetup: String,
    @JsonProperty("abandonedCartReminder") val abandonedCartReminder: String,
    @JsonProperty("abandonedCartDelayHours") val abandonedCartDelayHours: Int
) {
    constructor(settings: ShopSettings) : this(
        settings.orderConfirmationEmail.name,
        settings.paymentDelay.minutes,
        settings.freeShippingThreshold.price.amount.value,
        settings.sellerSetup.name,
        settings.abandonedCartReminder.activation.name,
        settings.abandonedCartDelay.hours
    )
}

// The price of the items, in euros, from which the standard shipping is free (0: always free)
data class JsonFreeShippingThreshold @JsonCreator constructor(@JsonProperty("amount") val amount: BigDecimal) {
    fun toFreeShippingThreshold() = FreeShippingThreshold(Price(Amount(amount), Currency.EUR))
}

// The time a cart is left before its customer is reminded of it, in hours
data class JsonAbandonedCartDelay @JsonCreator constructor(@JsonProperty("hours") val hours: Int) {
    fun toAbandonedCartDelay() = AbandonedCartDelay(hours)
}

// The time a customer has to pay an order once placed, in minutes
data class JsonPaymentDelay @JsonCreator constructor(@JsonProperty("minutes") val minutes: Int) {
    fun toPaymentDelay() = PaymentDelay(minutes)
}
