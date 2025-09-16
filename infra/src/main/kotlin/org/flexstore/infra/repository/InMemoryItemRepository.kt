package org.flexstore.infra.repository

import org.flexstore.domain.repository.ItemRepository
import org.flexstore.domain.valueobject.*
import java.math.BigDecimal
import java.util.concurrent.atomic.AtomicInteger

class InMemoryItemRepository: ItemRepository {

    val counter = AtomicInteger(0)

    override fun findAll(): List<Item> = items.toMutableList()

    override fun contains(newItem: Item): Boolean {
        return items.any { newItem is OneItem && it.itemId == newItem.itemId }
    }

    override fun remove(newItem: Item) {
        items.removeIf { newItem is OneItem && it.itemId == newItem.itemId }
    }

    override fun add(newItem: NewItem): OneItem {
        items.find { it.product.productId == newItem.product.productId }?.let {
            val oneItem = OneItem(it.itemId, newItem.product, newItem.quantity)
            update(oneItem)
            return oneItem
        }
        val oneItemToAdd = OneItem(
            itemId = ItemId(Identity("item-" + counter.incrementAndGet())),
            product = newItem.product,
            quantity = newItem.quantity
        )
        items.add(oneItemToAdd)
        return oneItemToAdd
    }

    override fun update(oneItem: OneItem) {
        val index = items.indexOfFirst { it.itemId == oneItem.itemId }
        if (index != -1) {
            if (oneItem.quantity.isNegative() || oneItem.quantity.isZero()) {
                items.removeAt(index)
            } else {
                items[index] = oneItem
            }
        }
    }

    override fun findById(itemId: ItemId): Item {
        return items.firstOrNull { it.itemId == itemId } ?: NoItem
    }

    override fun clear() = items.clear()

    private val items = mutableListOf<OneItem>(
        OneItem(
            itemId = ItemId(Identity("item-" + counter.incrementAndGet())),
            product = Product(
                productId = ProductId(Identity("prod-1")),
                name = Name("Mechanical Keyboard"),
                description = Description("A mechanical keyboard with RGB lighting"),
                category = Category.ELECTRONICS,
                price = Price(Amount(BigDecimal("89.99")), Currency.USD)
            ),
            quantity = Quantity(2)
        ),
        OneItem(
            itemId = ItemId(Identity("item-" + counter.incrementAndGet())),
            product = Product(
                productId = ProductId(Identity("prod-2")),
                description = Description("A high-precision optical mouse"),
                category = Category.ELECTRONICS,
                name = Name("Optical Mouse"),
                price = Price(Amount(BigDecimal("29.99")), Currency.USD)
            ),
            quantity = Quantity(1)
        )
    )
}