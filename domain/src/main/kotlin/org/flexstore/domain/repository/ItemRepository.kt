package org.flexstore.domain.repository

import org.flexstore.domain.valueobject.Item
import org.flexstore.domain.valueobject.ItemId
import org.flexstore.domain.valueobject.NewItem
import org.flexstore.domain.valueobject.OneItem

interface ItemRepository {

    fun findAll(): List<Item>
    fun contains(newItem: Item): Boolean
    fun remove(newItem: Item)
    fun add(newItem: NewItem): OneItem
    fun update(oneItem: OneItem)
    fun findById(itemId: ItemId): Item
    fun clear()
}