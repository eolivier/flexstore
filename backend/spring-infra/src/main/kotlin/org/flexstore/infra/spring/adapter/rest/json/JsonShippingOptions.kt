package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.order.*
import java.math.BigDecimal

// The shipping methods, with their cost for the cart, and the price of the items from which the standard
// shipping is free
data class JsonShippingOptions(
    @JsonProperty("options") val options: List<JsonShippingOption>,
    @JsonProperty("freeShippingThreshold") val freeShippingThreshold: BigDecimal,
    @JsonProperty("currency") val currency: String
) {
    constructor(shippingOptions: ShippingOptions) : this(
        shippingOptions.asList().map { JsonShippingOption(it.method.name, it.cost.amount.value) },
        shippingOptions.freeShippingThreshold.price.amount.value,
        shippingOptions.freeShippingThreshold.price.currency.symbol
    )
}

// method: STANDARD or EXPRESS
data class JsonShippingOption(
    @JsonProperty("method") val method: String,
    @JsonProperty("cost") val cost: BigDecimal
)
