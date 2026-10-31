package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.discount.PromoCode
import org.flexstore.domain.order.DiscountCodeEntered
import org.flexstore.domain.order.EnteredDiscountCode
import org.flexstore.domain.order.NoDiscountCodeEntered
import org.flexstore.domain.order.OrderAddresses
import org.flexstore.domain.order.OrderDelivery
import org.flexstore.domain.order.ShippingMethod

// The addresses of an order to place: without billing address, the order is billed to the delivery address.
// shippingMethod: STANDARD (when not given) or EXPRESS; discountCode: the discount code entered, if any
data class JsonOrderAddresses @JsonCreator constructor(
    @JsonProperty("deliveryAddress") val deliveryAddress: JsonPostalAddress,
    @JsonProperty("billingAddress") val billingAddress: JsonPostalAddress? = null,
    @JsonProperty("shippingMethod") val shippingMethod: String? = null,
    @JsonProperty("discountCode") val discountCode: String? = null
) {
    fun toEnteredDiscountCode(): EnteredDiscountCode {
        val typedCode = discountCode?.takeIf { it.isNotBlank() } ?: return NoDiscountCodeEntered
        return DiscountCodeEntered(PromoCode.from(typedCode))
    }

    fun toOrderDelivery(): OrderDelivery {
        val method = shippingMethod?.let { ShippingMethod.valueOf(it) } ?: ShippingMethod.STANDARD
        return OrderDelivery(toOrderAddresses(), method)
    }

    fun toOrderAddresses(): OrderAddresses {
        val delivery = deliveryAddress.toPostalAddress()
        return OrderAddresses(delivery, billingAddress?.toPostalAddress() ?: delivery)
    }
}
