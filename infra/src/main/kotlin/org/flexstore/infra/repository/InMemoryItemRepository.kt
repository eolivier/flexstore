package org.flexstore.infra.repository

import org.flexstore.domain.repository.ItemRepository
import org.flexstore.domain.valueobject.*
import java.math.BigDecimal
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

    private val items = mutableListOf<CartItem>(
        CartItem(
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
        CartItem(
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