package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.discount.PromoCode
import org.flexstore.domain.legal.LegalPageId
import org.flexstore.domain.order.AcceptedTermsOfSale
import org.flexstore.domain.order.DiscountCodeEntered
import org.flexstore.domain.order.EnteredDiscountCode
import org.flexstore.domain.order.NoDiscountCodeEntered
import org.flexstore.domain.order.NoTermsOfSaleAccepted
import org.flexstore.domain.order.OrderAddresses
import org.flexstore.domain.order.OrderDelivery
import org.flexstore.domain.order.ShippingMethod
import org.flexstore.domain.order.TermsOfSaleAcceptance
import org.flexstore.domain.sharedkernel.Identity

// The addresses of an order to place: without billing address, the order is billed to the delivery address.
// shippingMethod: STANDARD (when not given) or EXPRESS; discountCode: the discount code entered, if any;
// termsOfSaleId: the version of the terms of sale the customer read and accepted
data class JsonOrderAddresses @JsonCreator constructor(
    @JsonProperty("deliveryAddress") val deliveryAddress: JsonPostalAddress,
    @JsonProperty("billingAddress") val billingAddress: JsonPostalAddress? = null,
    @JsonProperty("shippingMethod") val shippingMethod: String? = null,
    @JsonProperty("discountCode") val discountCode: String? = null,
    @JsonProperty("termsOfSaleId") val termsOfSaleId: String? = null
) {
    fun toTermsOfSaleAcceptance(): TermsOfSaleAcceptance {
        val acceptedId = termsOfSaleId?.takeIf { it.isNotBlank() } ?: return NoTermsOfSaleAccepted
        return AcceptedTermsOfSale(LegalPageId(Identity(acceptedId)))
    }

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
