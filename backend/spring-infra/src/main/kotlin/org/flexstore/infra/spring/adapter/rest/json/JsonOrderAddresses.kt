package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.order.OrderAddresses

// The addresses of an order to place: without billing address, the order is billed to the delivery address
data class JsonOrderAddresses @JsonCreator constructor(
    @JsonProperty("deliveryAddress") val deliveryAddress: JsonPostalAddress,
    @JsonProperty("billingAddress") val billingAddress: JsonPostalAddress? = null
) {
    fun toOrderAddresses(): OrderAddresses {
        val delivery = deliveryAddress.toPostalAddress()
        return OrderAddresses(delivery, billingAddress?.toPostalAddress() ?: delivery)
    }
}
