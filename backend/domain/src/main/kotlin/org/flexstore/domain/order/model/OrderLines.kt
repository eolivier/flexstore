package org.flexstore.domain.order.model

import java.math.BigDecimal
import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*

data class OrderLines(private val lines: List<OrderLine>) {

    constructor(vararg lines: OrderLine) : this(lines.toList())

    fun totalPrice(): Price {
        val currency = lines.firstOrNull()?.unitPrice?.currency ?: Currency.EUR
        val total = lines.fold(BigDecimal.ZERO) { sum, line -> sum.add(line.linePrice().amount.value) }
        return Price(Amount(total), currency)
    }

    // The lines of the products sold at the given rate of VAT
    fun soldAt(vatRate: VatRate) = OrderLines(lines.filter { line -> line.vatRate == vatRate })

    fun isEmpty() = lines.isEmpty()

    fun includes(productId: ProductId) = lines.any { line -> line.productId == productId }

    // The products sold the most, by units: the first ones, then by revenue
    fun bestSellers(limit: BestSellerCount): BestSellers {
        val sales = lines.groupBy { line -> line.productId }.map { (_, productLines) ->
            val units = productLines.sumOf { line -> line.quantity.value }
            val revenue = OrderLines(productLines).totalPrice()
            BestSeller(productLines.last().let { line -> OrderedProduct(line.productId, line.productName) },
                Quantity(units), revenue)
        }
        val ranked = sales.sortedWith(
            compareByDescending<BestSeller> { it.unitsSold.value }.thenByDescending { it.revenue.amount.value }
        )
        return BestSellers(ranked.take(limit.value))
    }

    // Read-only view for the adapters (JSON, persistence)
    fun asList(): List<OrderLine> = lines.toList()

    companion object {
        fun from(cart: Cart) = OrderLines(cart.items.fold(emptyList()) { lines, product, quantity ->
            lines + OrderLine.of(product, quantity)
        })
    }
}
