package org.flexstore.domain.order

import org.flexstore.domain.cart.Cart
import org.flexstore.domain.discount.*
import org.flexstore.domain.settings.FreeShippingThreshold
import org.flexstore.domain.sharedkernel.*
import java.math.BigDecimal

// How an order is shipped, chosen by the customer: standard (free from the threshold of the shop settings) or
// express, at its own cost
enum class ShippingMethod(private val cost: BigDecimal, private val label: ShippingLabel) {
    STANDARD(BigDecimal("4.90"), ShippingLabel("Livraison standard")),
    EXPRESS(BigDecimal("9.90"), ShippingLabel("Livraison express"));

    // How the customer reads it, on the payment page and in the emails (in French, the language of the shop)
    fun label() = label

    // What the shipping costs for the items ordered
    fun costFor(itemsPrice: Price, freeShippingThreshold: FreeShippingThreshold): Price {
        val freeShipping = this == STANDARD && freeShippingThreshold.isReachedBy(itemsPrice)
        val shippingCost = if (freeShipping) BigDecimal.ZERO else cost
        return Price(Amount(shippingCost), itemsPrice.currency)
    }
}

// The name of a shipping method, as the customer reads it
data class ShippingLabel(val value: String)

// Where the order goes, and how
data class OrderDelivery(val addresses: OrderAddresses, val method: ShippingMethod)

// How the order is shipped, and what it costs, fixed when the order is placed
data class OrderShipping(val method: ShippingMethod, val cost: Price)

// What changes the price of the items of an order: its shipping, and its discount
data class OrderAdjustments(val shipping: OrderShipping, val discount: OrderDiscount) {

    // Without discount
    constructor(shipping: OrderShipping) : this(shipping, NoOrderDiscount)
}

// What the customer pays for an order: its lines, less its discount, then its shipping
data class OrderBill(val lines: OrderLines, val adjustments: OrderAdjustments) {

    val shipping get() = adjustments.shipping

    val discount get() = adjustments.discount

    fun itemsPrice() = lines.totalPrice()

    fun discountAmount() = discount.amountIn(itemsPrice().currency)

    fun totalPrice() = itemsPrice() - discountAmount() + shipping.cost
}

// The terms of the shop for an order: the threshold of free shipping, and the discount code chosen
data class OrderTerms(val freeShippingThreshold: FreeShippingThreshold, val discountChoice: DiscountChoice) {

    // Without discount code
    constructor(freeShippingThreshold: FreeShippingThreshold) : this(freeShippingThreshold, WithoutDiscountCode)
}

// What an order is placed from: the cart of the customer, and the terms of the shop
data class OrderContent(val cart: Cart, val terms: OrderTerms) {

    // Without discount code
    constructor(cart: Cart, freeShippingThreshold: FreeShippingThreshold) :
        this(cart, OrderTerms(freeShippingThreshold))

    val freeShippingThreshold get() = terms.freeShippingThreshold

    // The discount of the items: what the code chosen takes off their price
    fun discountOn(itemsPrice: Price): OrderDiscount = when (val discountChoice = terms.discountChoice) {
        WithoutDiscountCode -> NoOrderDiscount
        is WithDiscountCode -> {
            val discountCode = discountChoice.discountCode
            AppliedOrderDiscount(discountCode.code, discountCode.reduction.amountOff(itemsPrice))
        }
    }
}

// What a shipping method costs for the items of a cart
data class ShippingOption(val method: ShippingMethod, val cost: Price)

// The shipping methods a customer chooses from, with their cost for their cart, and the threshold from which the
// standard shipping is free
data class ShippingOptions(
    private val options: List<ShippingOption>,
    val freeShippingThreshold: FreeShippingThreshold
) {

    // Read-only view for the adapters (JSON)
    fun asList(): List<ShippingOption> = options.toList()

    companion object {
        // Every shipping method, at its cost for these items
        fun forItems(itemsPrice: Price, freeShippingThreshold: FreeShippingThreshold): ShippingOptions {
            val options = ShippingMethod.entries.map { method ->
                ShippingOption(method, method.costFor(itemsPrice, freeShippingThreshold))
            }
            return ShippingOptions(options, freeShippingThreshold)
        }
    }
}
