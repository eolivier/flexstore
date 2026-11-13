package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.loyalty.LoyaltyPurchase
import org.flexstore.domain.loyalty.LoyaltyStatement

// A paid order of the customer with the points it earned
data class JsonLoyaltyPurchase(
    @JsonProperty("orderNumber") val orderNumber: String,
    @JsonProperty("placedAt") val placedAt: String,
    @JsonProperty("points") val points: Int
) {
    companion object {
        fun of(purchase: LoyaltyPurchase) =
            JsonLoyaltyPurchase(purchase.orderNumber, purchase.placedAt.toString(), purchase.points.value)
    }
}

// The points of the loyalty programme of a customer: the total and the purchases that earned them
data class JsonLoyaltyStatement(
    @JsonProperty("points") val points: Int,
    @JsonProperty("purchases") val purchases: List<JsonLoyaltyPurchase>
) {
    companion object {
        fun of(statement: LoyaltyStatement) =
            JsonLoyaltyStatement(statement.total().value, statement.asList().map { JsonLoyaltyPurchase.of(it) })
    }
}
