package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.order.model.*
import org.flexstore.domain.sharedkernel.*
import java.math.BigDecimal

// A shipping method: its name and its cost in euros, freeShippingEligibility ELIGIBLE (free from the threshold of
// the shop settings) or NOT_ELIGIBLE (always at its cost), status ENABLED (proposed to the customers) or DISABLED
data class JsonShippingMethod(
    @JsonProperty("id") val id: String,
    @JsonProperty("label") val label: String,
    @JsonProperty("cost") val cost: BigDecimal,
    @JsonProperty("freeShippingEligibility") val freeShippingEligibility: String,
    @JsonProperty("status") val status: String
) {
    constructor(shippingMethod: ShippingMethod) : this(
        shippingMethod.id.id.value,
        shippingMethod.label().value,
        shippingMethod.cost.amount.value,
        shippingMethod.eligibility.name,
        shippingMethod.status.name
    )
}

// A shipping method added by an administrator: it is enabled
data class JsonNewShippingMethod @JsonCreator constructor(
    @JsonProperty("label") val label: String,
    @JsonProperty("cost") val cost: BigDecimal,
    @JsonProperty("freeShippingEligibility") val freeShippingEligibility: String
) {
    fun toShippingMethod(): ShippingMethod {
        val eligibility = FreeShippingEligibility.valueOf(freeShippingEligibility)
        val price = Price(Amount(cost), Currency.EUR)
        return ShippingMethod(ShippingMethodId.nextId(), ShippingLabel(label.trim()), price, eligibility)
    }
}

// What an administrator changes of a shipping method
data class JsonShippingMethodChange @JsonCreator constructor(
    @JsonProperty("label") val label: String,
    @JsonProperty("cost") val cost: BigDecimal,
    @JsonProperty("freeShippingEligibility") val freeShippingEligibility: String,
    @JsonProperty("status") val status: String
) {
    fun toShippingMethod(shippingMethodId: ShippingMethodId): ShippingMethod {
        val eligibility = FreeShippingEligibility.valueOf(freeShippingEligibility)
        val price = Price(Amount(cost), Currency.EUR)
        val shippingMethodStatus = ShippingMethodStatus.valueOf(status)
        return ShippingMethod(shippingMethodId, ShippingLabel(label.trim()), price, eligibility, shippingMethodStatus)
    }
}
