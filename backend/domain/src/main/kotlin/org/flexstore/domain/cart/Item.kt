package org.flexstore.domain.cart

import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import java.math.BigDecimal

// The items of a cart; a cart holds cart items (stored) and draft items (not stored yet), never NoItem
data class Items(private val items: List<Item>) {

    // Items(), Items(item1, item2): the caller does not wrap the items in a list
    constructor(vararg items: Item) : this(items.toList())

    init {
        require(items.none { it is NoItem }) { "A cart only holds cart items and draft items" }
    }

    fun totalPrice(): Price {
        val totalAmount = productItems().fold(BigDecimal.ZERO) { total, item ->
            total.add(item.itemPrice().amount.value)
        }
        return Price(Amount(totalAmount), currency())
    }

    fun currency(): Currency =
        if (hasItems()) productItems().first().product.price.currency else Currency.EUR

    fun contains(itemId: ItemId) = cartItems().contains(itemId)

    fun doesNotContain(itemId: ItemId) = cartItems().doesNotContain(itemId)

    // None when the item is not in the items
    fun quantityOf(itemId: ItemId): Quantity = cartItems().quantityOf(itemId)

    fun contains(productId: ProductId) = productItems().any { it.product.productId == productId }

    fun doesNotContain(productId: ProductId) = !contains(productId)

    // NoItem when the product is not in the items
    fun itemOf(productId: ProductId): Item =
        if (contains(productId)) productItems().first { it.product.productId == productId } else NoItem

    fun hasAtLeast(productId: ProductId, quantity: Quantity) =
        contains(productId) && quantityOf(productId).isAtLeast(quantity)

    // A product is in the items at most once: a product already there gets the new quantity
    fun withProduct(product: DefinedProduct, quantity: Quantity): Items {
        if (doesNotContain(product.productId)) return Items(items + DraftItem(product, quantity))
        val existingItem = itemOf(product.productId)
        return Items(items.map {
            when {
                it != existingItem -> it
                it is CartItem -> it.copy(product = product, quantity = quantity)
                it is DraftItem -> it.copy(product = product, quantity = quantity)
                else -> it
            }
        })
    }

    fun withQuantity(itemId: ItemId, quantity: Quantity) =
        Items(items.map { if (it is CartItem && it.itemId == itemId) it.copy(quantity = quantity) else it })

    fun without(itemId: ItemId) = Items(items.filterNot { it is CartItem && it.itemId == itemId })

    // Folds over the product and the quantity of each item
    fun <R> fold(initial: R, operation: (R, DefinedProduct, Quantity) -> R): R =
        productItems().fold(initial) { result, item -> operation(result, item.product, item.quantity) }

    fun isEmpty() = items.isEmpty()

    fun hasItems() = items.isNotEmpty()

    fun size() = items.size

    // The items already stored, i.e. with an id
    fun cartItems() = CartItems(items.filterIsInstance<CartItem>())

    // Read-only view for the adapters (JSON, persistence)
    fun asList(): List<Item> = items.toList()

    private fun productItems() = items.filterIsInstance<ProductItem>()

    private fun quantityOf(productId: ProductId): Quantity = when (val item = itemOf(productId)) {
        is ProductItem -> item.quantity
        is NoItem -> Quantity.none()
    }
}

// The stored items of a cart: those that have an id
data class CartItems(private val items: List<CartItem>) {

    constructor(vararg items: CartItem) : this(items.toList())

    fun contains(itemId: ItemId) = items.any { it.itemId == itemId }

    fun doesNotContain(itemId: ItemId) = !contains(itemId)

    // None when the item is not among the stored items
    fun quantityOf(itemId: ItemId): Quantity =
        if (contains(itemId)) items.first { it.itemId == itemId }.quantity else Quantity.none()

    // Read-only view of the ids for the adapters (persistence)
    fun itemIds(): Set<ItemId> = items.map { it.itemId }.toSet()
}

sealed interface Item {
    fun increase(): Item = this
    fun decrease(): Item = this
    fun changeQuantity(newQuantity: Quantity): Item = this
}

// An item of a product, stored (CartItem) or not yet (DraftItem)
sealed interface ProductItem : Item {
    val product: DefinedProduct
    val quantity: Quantity

    fun itemPrice() = product.currentPrice.multiply(quantity)
}

data class CartItem(
    val itemId: ItemId,
    override val product: DefinedProduct,
    override val quantity: Quantity,
) : ProductItem {
    override fun increase() = copy(quantity = quantity.increase())
    override fun decrease() = copy(quantity = quantity.decrease())
    override fun changeQuantity(newQuantity: Quantity) = copy(quantity = newQuantity)
}
data class DraftItem(
    override val product: DefinedProduct,
    override val quantity: Quantity,
) : ProductItem {
    override fun increase() = copy(quantity = quantity.increase())
    override fun decrease() = copy(quantity = quantity.decrease())
    override fun changeQuantity(newQuantity: Quantity) = copy(quantity = newQuantity)
}

data object NoItem : Item

data class ItemId(override val id: Identity) : Identifiable() {
    companion object {
        fun isValid(value: String?) = !value.isNullOrBlank()
    }
}
