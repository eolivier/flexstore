package org.flexstore.domain.repository

import org.flexstore.domain.valueobject.Item
import org.flexstore.domain.valueobject.ItemId
import org.flexstore.domain.valueobject.DraftItem
import org.flexstore.domain.valueobject.CartItem

interface ItemRepository {

    fun findAll(): List<Item>
    fun contains(newItem: Item): Boolean
    fun remove(newItem: Item)
    fun add(draftItem: DraftItem): CartItem
    fun update(cartItem: CartItem)
    fun findById(itemId: ItemId): Item
    fun clear()
}