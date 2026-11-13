package org.flexstore.domain.order.model

import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*

data class BestSellerCount(val value: Int)

// A product sold: the units of it sold, and what they brought
data class BestSeller(val product: OrderedProduct, val unitsSold: Quantity, val revenue: Price)

// The products sold the most, the best first
data class BestSellers(private val bestSellers: List<BestSeller>) {

    // Read-only view for the adapters (JSON)
    fun asList(): List<BestSeller> = bestSellers.toList()
}
