package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.order.*
import java.math.BigDecimal
import org.flexstore.domain.order.model.*

// The shipping methods, with their cost for the cart, and the price of the items from which the standard
// shipping is free
data class JsonShippingOptions(
    @JsonProperty("options") val options: List<JsonShippingOption>,
    @JsonProperty("freeShippingThreshold") val freeShippingThreshold: BigDecimal,
    @JsonProperty("currency") val currency: String
) {
    constructor(shippingOptions: ShippingOptions) : this(
        shippingOptions.asList().map {
            JsonShippingOption(it.method.id.id.value, it.method.label().value, it.cost.amount.value)
        },
        shippingOptions.freeShippingThreshold.price.amount.value,
        shippingOptions.freeShippingThreshold.price.currency.symbol
    )
}

// method: the id of the shipping method; label: its name, as the customer reads it
data class JsonShippingOption(
    @JsonProperty("method") val method: String,
    @JsonProperty("label") val label: String,
    @JsonProperty("cost") val cost: BigDecimal
)
