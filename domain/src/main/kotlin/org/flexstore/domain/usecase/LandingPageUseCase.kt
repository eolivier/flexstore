package org.flexstore.domain.usecase

import org.flexstore.domain.*

class LandingPageUseCase(private val command: Command) {

    fun changeQuantity(itemId: ItemId, newQuantity: Quantity) {
        modifyQuantity(itemId) { it.changeQuantity(newQuantity) }
    }

    fun increaseQuantity(itemId: ItemId) {
        modifyQuantity(itemId) { it.increase() }
    }

    fun decreaseQuantity(itemId: ItemId) {
        modifyQuantity(itemId) { it.decrease() }
    }

    fun modifyQuantity(itemId: ItemId, operation: (Item) -> Item) {
        val item = findBy(itemId)
        when (item) {
            is Item -> replaceItemWith(operation(item))
            is NoItem -> throw UnknownItemException("Unknown item id : $itemId")
        }
    }

    fun findBy(itemId: ItemId) = command.items.firstOrNull {
        it.itemId == itemId
    } ?: NoItem

    fun replaceItemWith(newItem: Item) {
        val index = command.items.indexOfFirst { it.itemId == newItem.itemId }
        if (index != -1) {
            command.items[index] = newItem
        }
    }
}
