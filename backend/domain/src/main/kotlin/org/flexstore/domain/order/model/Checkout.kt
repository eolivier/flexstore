package org.flexstore.domain.order.model

import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*

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
