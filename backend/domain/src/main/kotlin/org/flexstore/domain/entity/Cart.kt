package org.flexstore.domain.entity

import org.flexstore.domain.valueobject.*
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
data class Cart(val owner: CartOwner, val items: List<Item> = emptyList()) {

    init {
        require(items.none { it is NoItem }) { "A cart only holds cart items and draft items" }
    }

    fun add(product: DefinedProduct, quantity: Quantity): Cart {
        if (!quantity.isPositive()) {
            throw InvalidQuantityException(NonEmptyString("Quantity must be positive: ${quantity.value}"))
        }
        val index = items.indexOfFirst { productIdOf(it) == product.productId }
        if (index == -1) {
            return copy(items = items + DraftItem(product, quantity))
        }
        val updatedItem = when (val item = items[index]) {
            is CartItem -> item.copy(product = product, quantity = quantity)
            is DraftItem -> item.copy(product = product, quantity = quantity)
            is NoItem -> item
        }
        return copy(items = items.toMutableList().apply { set(index, updatedItem) })
    }

    // An unknown item (e.g. already removed from another page) leaves the cart unchanged
    fun changeQuantity(itemId: ItemId, quantity: Quantity): Cart {
        if (!quantity.isPositive()) {
            return remove(itemId)
        }
        return copy(items = items.map { if (it is CartItem && it.itemId == itemId) it.copy(quantity = quantity) else it })
    }

    fun remove(itemId: ItemId): Cart = copy(items = items.filterNot { it is CartItem && it.itemId == itemId })

    // The items of the other cart become new items of this cart
    fun mergeWith(other: Cart): Cart = other.items.fold(this) { cart, otherItem ->
        val (product, quantity) = when (otherItem) {
            is CartItem -> otherItem.product to otherItem.quantity
            is DraftItem -> otherItem.product to otherItem.quantity
            is NoItem -> return@fold cart
        }
        val currentQuantity = when (val item = cart.findBy(product.productId)) {
            is CartItem -> item.quantity
            is DraftItem -> item.quantity
            else -> null
        }
        if (currentQuantity != null && currentQuantity.value >= quantity.value) cart else cart.add(product, quantity)
    }

    fun findBy(itemId: ItemId): CartItem? = items.filterIsInstance<CartItem>().firstOrNull { it.itemId == itemId }

    fun findBy(productId: ProductId): Item? = items.firstOrNull { productIdOf(it) == productId }

    fun toItems() = Items(items)

    private fun productIdOf(item: Item): ProductId? = when (item) {
        is CartItem -> item.product.productId
        is DraftItem -> item.product.productId
        is NoItem -> null
    }
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
