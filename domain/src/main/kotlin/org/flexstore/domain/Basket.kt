package org.flexstore.domain

class Basket {

    private val items = mutableListOf<Item>()

    fun getItems() = items.toList()

    fun addItem(newItem: Item) {
        if (items.contains(newItem)) {
            items.remove(newItem)
        }
        items.add(newItem)
    }

    fun removeItem(item: Item) = items.remove(item)

    fun changeQuantity(itemId: ItemId, newQuantity: Quantity) {
        modifyQuantity(itemId) { it.changeQuantity(newQuantity) }
    }

    fun increaseQuantity(itemId: ItemId) = modifyQuantity(itemId) { it.increase() }

    fun decreaseQuantity(itemId: ItemId)  = modifyQuantity(itemId) { it.decrease() }

    fun modifyQuantity(itemId: ItemId, operation: (Item) -> Item) {
        val item = findBy(itemId)
        when (item) {
            is Item -> replaceItemWith(operation(item))
            is NoItem -> throw UnknownItemException("Unknown item id : $itemId")
        }
    }

    fun findBy(itemId: ItemId) = items.firstOrNull {
        it.itemId == itemId
    } ?: NoItem

    fun replaceItemWith(newItem: Item) {
        val index = items.indexOfFirst { it.itemId == newItem.itemId }
        if (index != -1) {
            items[index] = newItem
        }
    }
}

