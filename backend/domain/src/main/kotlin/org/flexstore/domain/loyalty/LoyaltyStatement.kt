package org.flexstore.domain.loyalty

import java.time.Instant
import org.flexstore.domain.order.model.Order
import org.flexstore.domain.order.model.Orders
import org.flexstore.domain.sharedkernel.Quantity

// The points earned by one paid order of the customer
data class LoyaltyPurchase(val orderNumber: String, val placedAt: Instant, val points: LoyaltyPoints) {

    companion object {
        fun of(order: Order): LoyaltyPurchase {
            val wholeUnitsSpent = Quantity(order.totalPrice().wholeUnits().value)
            return LoyaltyPurchase(order.number(), order.placedAt, LoyaltyPoints.earnedFor(wholeUnitsSpent))
        }
    }
}

// The purchases of a customer with the points each one earned, newest first, and the total of their points
data class LoyaltyStatement(private val purchases: List<LoyaltyPurchase>) {

    fun asList(): List<LoyaltyPurchase> = purchases.toList()

    fun total() = purchases.fold(LoyaltyPoints.none) { total, purchase -> total + purchase.points }

    companion object {
        // A line for each paid order (a refunded order is not counted)
        fun of(orders: Orders) =
            LoyaltyStatement(orders.paid().newestFirst().asList().map { order -> LoyaltyPurchase.of(order) })
    }
}
