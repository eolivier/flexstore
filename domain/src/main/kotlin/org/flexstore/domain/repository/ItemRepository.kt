package org.flexstore.domain.repository

import org.flexstore.domain.valueobject.Item
import org.flexstore.domain.valueobject.ItemId

interface ItemRepository {

    fun findAll(): List<Item>
    fun contains(newItem: Item): Boolean
    fun remove(newItem: Item)
    fun add(newItem: Item)
    fun findById(itemId: ItemId): Item
}