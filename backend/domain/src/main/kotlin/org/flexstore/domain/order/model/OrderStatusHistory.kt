package org.flexstore.domain.order.model

import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*

// The statuses an order went through, oldest first; never empty: an order is placed awaiting its payment
data class OrderStatusHistory(private val changes: List<OrderStatusChange>) {

    init {
        require(changes.isNotEmpty()) { "An order has at least the status it was placed with" }
    }

    constructor(vararg changes: OrderStatusChange) : this(changes.toList())

    fun currentStatus() = changes.last().status

    fun with(change: OrderStatusChange) = OrderStatusHistory(changes + change)

    // Read-only view for the adapters (JSON, persistence)
    fun asList(): List<OrderStatusChange> = changes.toList()
}
