package org.flexstore.domain.entity

import org.flexstore.domain.valueobject.*
import org.ucop.domain.NominalException
import org.ucop.domain.NonEmptyString

/**
 * Cart aggregate: its items and the cart rules, without any repository (the use cases load and save it).
 * - an item holds the catalog product given by the use case
 * - a product is in the cart at most once: adding it again changes the quantity of its item
 * - an item whose quantity becomes zero or negative is removed
 * New items are DraftItem until the cart is saved.
 */
data class Cart(val items: List<Item> = emptyList()) {

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
