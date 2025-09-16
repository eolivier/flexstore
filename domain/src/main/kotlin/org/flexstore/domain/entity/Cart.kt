package org.flexstore.domain.entity

import org.flexstore.domain.repository.ItemRepository
import org.flexstore.domain.valueobject.*

class Cart(private val itemRepository: ItemRepository) {

    fun getItems() = itemRepository.findAll()

    fun add(newItem: NewItem) = itemRepository.add(newItem)

    fun update(oneItem: OneItem) = itemRepository.update(oneItem)

    fun removeItem(item: Item) = itemRepository.remove(item)

    fun changeQuantity(item: Item, newQuantity: Quantity) {
        modifyQuantity(item) { it.changeQuantity(newQuantity) }
    }

    fun increaseQuantity(item: Item) = modifyQuantity(item) { it.increase() }

    fun decreaseQuantity(item: Item)  = modifyQuantity(item) { it.decrease() }

    fun modifyQuantity(item: Item, operation: (Item) -> Item) {
        when (item) {
            is NewItem -> add(operation(item) as NewItem)
            is OneItem -> update(operation(item) as OneItem)
            is NoItem -> throw UnknownItemException("Unknown item : $item")
        }
    }

    fun findBy(itemId: ItemId) = itemRepository.findById(itemId) ?: NoItem
}

