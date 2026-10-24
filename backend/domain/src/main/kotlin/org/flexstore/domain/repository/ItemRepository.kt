package org.flexstore.domain.repository

import org.flexstore.domain.valueobject.*

/**
 * Storage of the cart items. The cart rules (one item per product, removal at zero...) are in Cart.
 */
interface ItemRepository {

    fun getItems(): Items
    fun findAll(): List<Item>
    fun contains(newItem: Item): Boolean
    fun remove(newItem: Item)
    // Stores a new item and gives it an id
    fun add(draftItem: DraftItem): CartItem
    // Stores the quantity of an existing item
    fun update(cartItem: CartItem)
    fun findById(itemId: ItemId): Item
    fun findByProductId(productId: ProductId): CartItem?
    fun clear()
}