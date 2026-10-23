package org.flexstore.infra.repository

import org.flexstore.domain.repository.ItemRepository
import org.flexstore.domain.valueobject.*
import java.util.concurrent.atomic.AtomicInteger

class InMemoryItemRepository: ItemRepository {

    val counter = AtomicInteger(0)

    override fun getItems(): Items = Items(findAll())

    override fun findAll(): List<Item> = items.toMutableList()

    override fun contains(newItem: Item): Boolean {
        return items.any { newItem is CartItem && it.itemId == newItem.itemId }
    }

    override fun remove(newItem: Item) {
        items.removeIf { newItem is CartItem && it.itemId == newItem.itemId }
    }

    override fun add(draftItem: DraftItem): CartItem {
        items.find { it.product.productId == draftItem.product.productId }?.let {
            val cartItem = CartItem(it.itemId, draftItem.product, draftItem.quantity)
            update(cartItem)
            return cartItem
        }
        val cartItemToAdd = CartItem(
            itemId = ItemId(Identity("item-" + counter.incrementAndGet())),
            product = draftItem.product,
            quantity = draftItem.quantity
        )
        items.add(cartItemToAdd)
        return cartItemToAdd
    }

    override fun update(cartItem: CartItem) {
        val index = items.indexOfFirst { it.itemId == cartItem.itemId }
        if (index != -1) {
            if (cartItem.quantity.isNegative() || cartItem.quantity.isZero()) {
                items.removeAt(index)
            } else {
                items[index] = CartItem(cartItem.itemId, cartItem.product, cartItem.quantity)
            }
        }
    }

    override fun findById(itemId: ItemId): Item {
        return items.firstOrNull { it.itemId == itemId } ?: NoItem
    }

    override fun clear() = items.clear()

    // Demo cart made of catalog products, so that it has the same currency as the catalog
    private val items = mutableListOf<CartItem>(
        CartItem(
            itemId = ItemId(Identity("item-" + counter.incrementAndGet())),
            product = clothes.first(),
            quantity = Quantity(2)
        ),
        CartItem(
            itemId = ItemId(Identity("item-" + counter.incrementAndGet())),
            product = electronics.first(),
            quantity = Quantity(1)
        )
    )
}