package org.flexstore.domain.order.model

import java.time.Instant
import org.flexstore.domain.cart.*
import org.flexstore.domain.legal.LegalPageId
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*

// An order to place: its stamp (id and date) and its checkout (customer and addresses)
data class OrderPlacement(val stamp: OrderStamp, val checkout: Checkout) {

    val orderId get() = stamp.orderId

    val placedAt get() = stamp.placedAt

    val customer get() = checkout.customer

    val addresses get() = checkout.addresses
}

// The id of a new order (chosen by the caller, which can then read the order) and the date it is placed
data class OrderStamp(val orderId: OrderId, val placedAt: Instant)

// What the customer chooses when checking out: who orders, where the order is delivered and billed, how it is
// shipped, and the terms of sale they accept
data class Checkout(
    val customer: CartOwner.Customer,
    val delivery: OrderDelivery,
    val termsOfSale: TermsOfSaleAcceptance
) {

    // Shipped the standard way
    constructor(customer: CartOwner.Customer, addresses: OrderAddresses, termsOfSale: TermsOfSaleAcceptance) :
        this(customer, OrderDelivery(addresses, ShippingMethod.STANDARD), termsOfSale)

    val addresses get() = delivery.addresses
}

// Where the order is delivered and to which address it is billed (often the same)
data class OrderAddresses(val delivery: PostalAddress, val billing: PostalAddress) {

    fun isComplete() = delivery.isComplete() && billing.isComplete()

    fun isIncomplete() = !isComplete()

    fun isBilledToTheDeliveryAddress() = delivery == billing

    companion object {
        fun sameFor(address: PostalAddress) = OrderAddresses(address, address)
    }
}

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
