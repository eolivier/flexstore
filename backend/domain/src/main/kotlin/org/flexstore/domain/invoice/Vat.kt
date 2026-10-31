package org.flexstore.domain.invoice

import org.flexstore.domain.sharedkernel.*
import java.math.BigDecimal
import java.math.RoundingMode

// A rate of VAT, in percent
data class VatRate(val percent: BigDecimal) {
    companion object {
        // The standard French rate: every price of the shop includes it
        fun standard() = VatRate(BigDecimal(20))
    }
}

// A price that includes the VAT, split into its amount without VAT and its VAT (rounded to the cent)
data class VatBreakdown(val priceIncludingVat: Price, val rate: VatRate) {

    fun priceExcludingVat(): Price {
        val divisor = BigDecimal.ONE.add(rate.percent.movePointLeft(2))
        val amount = priceIncludingVat.amount.value.divide(divisor, 2, RoundingMode.HALF_UP)
        return Price(Amount(amount), priceIncludingVat.currency)
    }

    fun vat(): Price {
        val amount = priceIncludingVat.amount.value.subtract(priceExcludingVat().amount.value)
        return Price(Amount(amount), priceIncludingVat.currency)
    }
}
