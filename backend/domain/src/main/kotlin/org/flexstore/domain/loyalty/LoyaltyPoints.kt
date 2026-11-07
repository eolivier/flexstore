package org.flexstore.domain.loyalty

import org.flexstore.domain.sharedkernel.Quantity

// The points of the loyalty programme of a customer: one point for each whole euro or dollar they spent
data class LoyaltyPoints(val value: Int) {

    companion object {
        fun earnedFor(wholeUnitsSpent: Quantity) = LoyaltyPoints(wholeUnitsSpent.value)
    }
}
