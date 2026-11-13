package org.flexstore.domain.order.model

import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*

// A product as ordered, with its unit price and its rate of VAT when the order was placed
data class PricedProduct(val product: OrderedProduct, val pricing: ProductPricing) {

    // At the standard rate of VAT
    constructor(product: OrderedProduct, unitPrice: Price) : this(product, ProductPricing(unitPrice))

    val unitPrice get() = pricing.price

    val vatRate get() = pricing.vatRate
}
