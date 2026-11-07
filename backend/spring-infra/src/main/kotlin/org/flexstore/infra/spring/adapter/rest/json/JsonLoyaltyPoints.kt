package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.loyalty.LoyaltyPoints

// The points of the loyalty programme of a customer
data class JsonLoyaltyPoints(@JsonProperty("points") val points: Int) {
    companion object {
        fun of(loyaltyPoints: LoyaltyPoints) = JsonLoyaltyPoints(loyaltyPoints.value)
    }
}
