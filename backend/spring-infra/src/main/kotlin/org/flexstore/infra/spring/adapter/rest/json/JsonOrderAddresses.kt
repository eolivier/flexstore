package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.discount.PromoCode
import org.flexstore.domain.legal.LegalPageId
import org.flexstore.domain.order.model.AcceptedTermsOfSale
import org.flexstore.domain.order.model.DiscountCodeEntered
import org.flexstore.domain.order.model.EnteredDiscountCode
import org.flexstore.domain.order.model.NoDiscountCodeEntered
import org.flexstore.domain.order.model.NoTermsOfSaleAccepted
import org.flexstore.domain.order.model.OrderAddresses
import org.flexstore.domain.order.model.OrderDelivery
import org.flexstore.domain.order.model.ShippingMethod
import org.flexstore.domain.order.model.ShippingMethodId
import org.flexstore.domain.order.model.TermsOfSaleAcceptance
import org.flexstore.domain.sharedkernel.Identity

// The addresses of an order to place: without billing address, the order is billed to the delivery address.
// shippingMethod: the id of the shipping method chosen (STANDARD when not given); discountCode: the discount code
// entered, if any; termsOfSaleId: the version of the terms of sale the customer read and accepted
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

    fun toShippingMethodId(): ShippingMethodId {
        val chosenId = shippingMethod?.takeIf { it.isNotBlank() } ?: return ShippingMethod.STANDARD.id
        return ShippingMethodId(Identity(chosenId))
    }

    fun toOrderDelivery(shippingMethod: ShippingMethod) = OrderDelivery(toOrderAddresses(), shippingMethod)

    fun toOrderAddresses(): OrderAddresses {
        val delivery = deliveryAddress.toPostalAddress()
        return OrderAddresses(delivery, billingAddress?.toPostalAddress() ?: delivery)
    }
}
