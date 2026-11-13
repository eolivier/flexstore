package org.flexstore.domain.order.model

import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*

// The products sold the most, the best first
data class BestSellers(private val bestSellers: List<BestSeller>) {

    // Read-only view for the adapters (JSON)
    fun asList(): List<BestSeller> = bestSellers.toList()
}
