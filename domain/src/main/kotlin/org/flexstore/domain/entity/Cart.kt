package org.flexstore.domain.entity

import org.flexstore.domain.repository.ItemRepository
import org.flexstore.domain.valueobject.*

class Cart(private val itemRepository: ItemRepository) {

    fun getItems() = itemRepository.findAll()

    fun save(item: Item)  {
        when (item) {
            is DraftItem -> add(item)
            is CartItem -> update(item)
            is NoItem -> throw UnknownItemException("Unknown item : $item")
        }
    }

    fun add(draftItem: DraftItem) = itemRepository.add(draftItem)

    fun update(cartItem: CartItem) = itemRepository.update(cartItem)

    fun removeItem(item: Item) = itemRepository.remove(item)

    fun changeQuantity(item: Item, newQuantity: Quantity) {
        modifyQuantity(item) { it.changeQuantity(newQuantity) }
    }

    fun increaseQuantity(item: Item) = modifyQuantity(item) { it.increase() }

    fun decreaseQuantity(item: Item)  = modifyQuantity(item) { it.decrease() }

    fun modifyQuantity(item: Item, operation: (Item) -> Item) {
        when (item) {
            is DraftItem -> add(operation(item) as DraftItem)
            is CartItem -> update(operation(item) as CartItem)
            is NoItem -> throw UnknownItemException("Unknown item : $item")
        }
    }

    fun findBy(itemId: ItemId) = itemRepository.findById(itemId) ?: NoItem
}

