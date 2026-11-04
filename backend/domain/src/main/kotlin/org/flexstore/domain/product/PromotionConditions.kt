package org.flexstore.domain.product

import org.ucop.domain.ErrorMessage

// The promotion of a created or edited product: a percentage of more than 0 and 100 at most, during a period
// that ends after it starts

internal fun throwIfInvalid(promotion: Promotion) {
    if (promotion.isInvalid()) {
        val rule = "a percentage from more than 0 to 100, from a start to a later end"
        throw InvalidPromotionException(ErrorMessage("The promotion is invalid: $rule."))
    }
}
