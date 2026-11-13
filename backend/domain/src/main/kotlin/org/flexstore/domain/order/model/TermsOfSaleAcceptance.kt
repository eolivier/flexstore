package org.flexstore.domain.order.model

import org.flexstore.domain.cart.*
import org.flexstore.domain.legal.LegalPageId
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*

// The terms of sale the customer accepted to place an order: the version they read. The orders placed before
// the customers accepted the terms of sale have none
sealed interface TermsOfSaleAcceptance {

    // The version accepted is no longer the current one of the shop
    fun isOutdatedComparedTo(currentTermsOfSale: LegalPageId): Boolean
}

data class AcceptedTermsOfSale(val termsOfSaleId: LegalPageId) : TermsOfSaleAcceptance {

    override fun isOutdatedComparedTo(currentTermsOfSale: LegalPageId) = termsOfSaleId != currentTermsOfSale
}

data object NoTermsOfSaleAccepted : TermsOfSaleAcceptance {

    // Nothing accepted: nothing is outdated, the order is refused for lack of acceptance
    override fun isOutdatedComparedTo(currentTermsOfSale: LegalPageId) = false
}
