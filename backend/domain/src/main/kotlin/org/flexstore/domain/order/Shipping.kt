package org.flexstore.domain.order

import org.flexstore.domain.cart.Cart
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

// What the customer pays for an order: its lines, then its shipping
data class OrderBill(val lines: OrderLines, val shipping: OrderShipping) {

    fun itemsPrice() = lines.totalPrice()

    fun totalPrice() = itemsPrice() + shipping.cost
}

// What an order is placed from: the cart of the customer, and the threshold of free shipping of the shop
data class OrderContent(val cart: Cart, val freeShippingThreshold: FreeShippingThreshold)

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
