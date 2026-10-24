package org.flexstore.domain.cart.model

import org.flexstore.domain.product.*
import org.flexstore.domain.shared.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.NominalException
import org.ucop.domain.NonEmptyString
import java.util.UUID

/**
 * Cart aggregate, identified by its owner: its items and the cart rules, without any repository
 * (the use cases load and save it).
 * - an item holds the catalog product given by the use case
 * - a product is in the cart at most once: adding it again changes the quantity of its item
 * - an item whose quantity becomes zero or negative is removed
 * - merging a cart keeps all the items; for a product in both carts, the larger quantity is kept
 * New items are DraftItem until the cart is saved.
 */
data class Cart(val owner: CartOwner, val items: Items = Items()) {

    fun add(product: DefinedProduct, quantity: Quantity): Cart {
        if (quantity.isZeroOrNegative()) {
            throw InvalidQuantityException(NonEmptyString("Quantity must be positive: ${quantity.value}"))
        }
        return copy(items = items.withProduct(product, quantity))
    }

    // An unknown item (e.g. already removed from another page) leaves the cart unchanged
    fun changeQuantity(itemId: ItemId, quantity: Quantity): Cart {
        if (quantity.isZeroOrNegative()) {
            return remove(itemId)
        }
        return copy(items = items.withQuantity(itemId, quantity))
    }

    fun remove(itemId: ItemId): Cart = copy(items = items.without(itemId))

    // The items of the other cart become new items of this cart
    fun mergeWith(other: Cart): Cart = other.items.fold(this) { cart, product, quantity ->
        val currentQuantity = cart.items.quantityOf(product.productId)
        if (currentQuantity?.isAtLeast(quantity) == true) cart else cart.add(product, quantity)
    }

    fun findBy(itemId: ItemId): CartItem? = items.findBy(itemId)

    fun findBy(productId: ProductId): Item? = items.findBy(productId)

    fun isEmpty() = items.isEmpty()

    fun hasItems() = items.hasItems()

    fun emptied() = copy(items = Items())
}

data class InvalidQuantityException(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)

/**
 * The owner of a cart: a signed-in customer, or a visitor identified by a cart id kept by the client.
 * A customer keeps the same cart on every device.
 */
sealed interface CartOwner {
    data class Customer(val userId: UserId.ValidUserId) : CartOwner
    data class Visitor(val cartId: VisitorCartId) : CartOwner
}

data class VisitorCartId(val value: String) {
    init {
        require(isValid(value)) { "A visitor cart id is a UUID: $value" }
    }

    companion object {
        fun nextId() = VisitorCartId(Identity.nextId().value)

        fun isValid(value: String?) = value != null && runCatching { UUID.fromString(value) }.isSuccess
    }
}
