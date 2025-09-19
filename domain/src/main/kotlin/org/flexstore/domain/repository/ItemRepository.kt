package org.flexstore.domain.repository

import org.flexstore.domain.valueobject.*

interface ItemRepository {

    fun getItems(): Items
    fun findAll(): List<Item>
    fun contains(newItem: Item): Boolean
    fun remove(newItem: Item)
    fun add(draftItem: DraftItem): CartItem
    fun update(cartItem: CartItem)
    fun findById(itemId: ItemId): Item
    fun clear()
}