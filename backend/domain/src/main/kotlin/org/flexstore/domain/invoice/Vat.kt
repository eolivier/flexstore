package org.flexstore.domain.invoice

import org.flexstore.domain.order.Order
import org.flexstore.domain.sharedkernel.*
import java.math.BigDecimal
import java.math.RoundingMode

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

// The VAT of an order, rate by rate, from the highest: the items sold at each rate, with their share of the
// shipping and of the discount, in proportion to their price (the costs that come with a sale follow the VAT
// of what is sold). The last rate takes what the rounding leaves, so that the rates add up to the total of the
// order
data class VatBreakdowns(private val breakdowns: List<VatBreakdown>) {

    fun priceExcludingVat(): Price = breakdowns.map { it.priceExcludingVat() }.reduce(Price::plus)

    // Read-only view for the document of the invoice
    fun asList(): List<VatBreakdown> = breakdowns.toList()

    companion object {
        fun of(order: Order): VatBreakdowns {
            val itemsPrice = order.itemsPrice()
            val adjustments = order.totalPrice() - itemsPrice
            val soldRates = VatRate.entries.filterNot { rate -> order.lines.soldAt(rate).isEmpty() }
            val shares = soldRates.dropLast(1).map { rate ->
                val ratePrice = order.lines.soldAt(rate).totalPrice()
                rate to ratePrice + shareOf(adjustments, ratePrice, itemsPrice)
            }
            val lastRate = soldRates.last()
            val lastPrice = shares.fold(order.totalPrice()) { rest, (_, sharePrice) -> rest - sharePrice }
            val pricesByRate = shares + (lastRate to lastPrice)
            val breakdowns = pricesByRate.map { (rate, price) -> VatBreakdown(price, rate) }
            return VatBreakdowns(breakdowns)
        }

        // The part of the adjustments that comes with the items of a rate, rounded to the cent
        private fun shareOf(adjustments: Price, ratePrice: Price, itemsPrice: Price): Price {
            if (itemsPrice.isFree()) return Price(Amount(BigDecimal.ZERO), adjustments.currency)
            val weighted = adjustments.amount.value.multiply(ratePrice.amount.value)
            val share = weighted.divide(itemsPrice.amount.value, 2, RoundingMode.HALF_UP)
            return Price(Amount(share), adjustments.currency)
        }
    }
}
