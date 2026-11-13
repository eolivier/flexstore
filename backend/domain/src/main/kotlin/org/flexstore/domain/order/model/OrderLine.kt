package org.flexstore.domain.order.model

import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*

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
