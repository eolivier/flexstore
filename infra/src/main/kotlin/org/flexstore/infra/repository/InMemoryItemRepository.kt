package org.flexstore.infra.repository

import org.flexstore.domain.repository.ItemRepository
import org.flexstore.domain.valueobject.*
import org.ucop.domain.entity.Name
import java.math.BigDecimal

class InMemoryItemRepository: ItemRepository {

    override fun findAll(): List<Item> = items.toMutableList()

    override fun contains(newItem: Item): Boolean {
        return items.any { newItem is OneItem && it.itemId == newItem.itemId }
    }

    override fun remove(newItem: Item) {
        items.removeIf { newItem is OneItem && it.itemId == newItem.itemId }
    }

    override fun add(newItem: Item) {
        items.add(newItem as OneItem)
    }

    override fun findById(itemId: ItemId): Item {
        return items.firstOrNull { it.itemId == itemId } ?: NoItem
    }

    private val items = mutableListOf<OneItem>(
        OneItem(
            itemId = ItemId(Identity("item-1")),
            product = Product(
                productId = ProductId(Identity("prod-1")),
                name = Name("Mechanical Keyboard"),
                price = Price(Amount(BigDecimal("89.99")), Currency.USD)
            ),
            quantity = Quantity(2)
        ),
        OneItem(
            itemId = ItemId(Identity("item-2")),
            product = Product(
                productId = ProductId(Identity("prod-2")),
                name = Name("Optical Mouse"),
                price = Price(Amount(BigDecimal("29.99")), Currency.USD)
            ),
            quantity = Quantity(1)
        )
    )
}