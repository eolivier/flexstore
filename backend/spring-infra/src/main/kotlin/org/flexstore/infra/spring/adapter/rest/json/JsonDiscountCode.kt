package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.discount.*
import org.flexstore.domain.order.DiscountPreview
import org.flexstore.domain.sharedkernel.*
import java.math.BigDecimal
import java.time.Instant

// A discount code: reductionType PERCENTAGE (reductionValue from 1 to 100) or AMOUNT (reductionValue in euros),
// usable until expiresAt by usageLimit orders, timesUsed already
data class JsonDiscountCode(
    @JsonProperty("code") val code: String,
    @JsonProperty("reductionType") val reductionType: String,
    @JsonProperty("reductionValue") val reductionValue: BigDecimal,
    @JsonProperty("expiresAt") val expiresAt: String,
    @JsonProperty("usageLimit") val usageLimit: Int,
    @JsonProperty("timesUsed") val timesUsed: Int
) {
    constructor(discountCode: DiscountCode) : this(
        discountCode.code.value,
        reductionTypeOf(discountCode.reduction),
        reductionValueOf(discountCode.reduction),
        discountCode.offer.validity.expiresAt.value.toString(),
        discountCode.offer.validity.usageLimit.value,
        discountCode.uses.value
    )
}

// A discount code given by an administrator; the code is put in upper case
data class JsonNewDiscountCode @JsonCreator constructor(
    @JsonProperty("code") val code: String,
    @JsonProperty("reductionType") val reductionType: String,
    @JsonProperty("reductionValue") val reductionValue: BigDecimal,
    @JsonProperty("expiresAt") val expiresAt: String,
    @JsonProperty("usageLimit") val usageLimit: Int
) {
    fun toDiscountCode(): DiscountCode {
        val reduction = when (reductionType) {
            "PERCENTAGE" -> PercentageOff(Percentage(reductionValue))
            else -> AmountOff(Price(Amount(reductionValue), Currency.EUR))
        }
        val validity = DiscountValidity(ExpiryDate(Instant.parse(expiresAt)), UsageLimit(usageLimit))
        return DiscountCode(PromoCode.from(code), DiscountOffer(reduction, validity))
    }
}

// What a discount code entered takes off the cart of the customer
data class JsonDiscountPreview(
    @JsonProperty("code") val code: String,
    @JsonProperty("amountOff") val amountOff: BigDecimal,
    @JsonProperty("currency") val currency: String
) {
    constructor(discountPreview: DiscountPreview) : this(
        discountPreview.code.value,
        discountPreview.amountOff.amount.value,
        discountPreview.amountOff.currency.symbol
    )
}

private fun reductionTypeOf(reduction: Reduction) = when (reduction) {
    is PercentageOff -> "PERCENTAGE"
    is AmountOff -> "AMOUNT"
}

private fun reductionValueOf(reduction: Reduction) = when (reduction) {
    is PercentageOff -> reduction.percentage.value
    is AmountOff -> reduction.amount.amount.value
}
