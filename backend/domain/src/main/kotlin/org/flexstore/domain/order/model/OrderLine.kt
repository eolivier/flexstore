package org.flexstore.domain.order.model

import java.math.BigDecimal
import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*

// A product as ordered: its id and its name when the order was placed
data class OrderedProduct(val productId: ProductId, val productName: Name)

// A product as ordered, with its unit price and its rate of VAT when the order was placed
data class PricedProduct(val product: OrderedProduct, val pricing: ProductPricing) {

    // At the standard rate of VAT
    constructor(product: OrderedProduct, unitPrice: Price) : this(product, ProductPricing(unitPrice))

    val unitPrice get() = pricing.price

    val vatRate get() = pricing.vatRate
}

// A line of an order: a product, at its price when the order was placed, and its quantity
data class OrderLine(val product: PricedProduct, val quantity: Quantity) {

    val productId get() = product.product.productId

    val productName get() = product.product.productName

    val unitPrice get() = product.unitPrice

    val vatRate get() = product.vatRate

    fun linePrice() = unitPrice.multiply(quantity)

    companion object {
        // The line of a product of the catalog, with its name, its price (promotion included) and its rate
        // of VAT at this time
        fun of(definedProduct: DefinedProduct, quantity: Quantity): OrderLine {
            val orderedProduct = OrderedProduct(definedProduct.productId, definedProduct.name)
            val pricing = ProductPricing(definedProduct.currentPrice, definedProduct.vatRate)
            return OrderLine(PricedProduct(orderedProduct, pricing), quantity)
        }
    }
}

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
