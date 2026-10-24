package org.flexstore.domain.cart.model

import org.flexstore.domain.product.*
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
        val totalAmount = items.fold(BigDecimal.ZERO) { total, item -> total.add(priceOf(item).amount.value) }
        return Price(Amount(totalAmount), currency())
    }

    fun currency(): Currency = items.firstOrNull()?.let { productOf(it)?.price?.currency } ?: Currency.EUR

    fun findBy(itemId: ItemId): CartItem? = cartItems().findBy(itemId)

    fun findBy(productId: ProductId): Item? = items.firstOrNull { productOf(it)?.productId == productId }

    fun quantityOf(productId: ProductId): Quantity? = findBy(productId)?.let { quantityOf(it) }

    // A product is in the items at most once: a product already there gets the new quantity
    fun withProduct(product: DefinedProduct, quantity: Quantity): Items {
        val existingItem = findBy(product.productId) ?: return Items(items + DraftItem(product, quantity))
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
        items.fold(initial) { result, item -> operation(result, productOf(item)!!, quantityOf(item)!!) }

    fun isEmpty() = items.isEmpty()

    fun hasItems() = items.isNotEmpty()

    fun size() = items.size

    // The items already stored, i.e. with an id
    fun cartItems() = CartItems(items.filterIsInstance<CartItem>())

    // Read-only view for the adapters (JSON, persistence)
    fun asList(): List<Item> = items.toList()

    private fun productOf(item: Item): DefinedProduct? = when (item) {
        is CartItem -> item.product
        is DraftItem -> item.product
        is NoItem -> null
    }

    private fun quantityOf(item: Item): Quantity? = when (item) {
        is CartItem -> item.quantity
        is DraftItem -> item.quantity
        is NoItem -> null
    }

    private fun priceOf(item: Item): Price = when (item) {
        is CartItem -> item.itemPrice()
        is DraftItem -> item.itemPrice()
        is NoItem -> Price(Amount(BigDecimal.ZERO), Currency.EUR)
    }
}

// The stored items of a cart: those that have an id
data class CartItems(private val items: List<CartItem>) {

    constructor(vararg items: CartItem) : this(items.toList())

    fun findBy(itemId: ItemId): CartItem? = items.firstOrNull { it.itemId == itemId }

    // Read-only view of the ids for the adapters (persistence)
    fun itemIds(): Set<ItemId> = items.map { it.itemId }.toSet()
}

sealed interface Item {
    fun increase(): Item = this
    fun decrease(): Item = this
    fun changeQuantity(newQuantity: Quantity): Item = this
}

data class CartItem(
    val itemId: ItemId,
    val product: DefinedProduct,
    val quantity: Quantity,
) : Item {
    override fun increase() = copy(quantity = quantity.increase())
    override fun decrease() = copy(quantity = quantity.decrease())
    override fun changeQuantity(newQuantity: Quantity) = copy(quantity = newQuantity)
    fun itemPrice() = product.price.multiply(quantity)
}
data class DraftItem(
    val product: DefinedProduct,
    val quantity: Quantity,
) : Item {
    override fun increase() = copy(quantity = quantity.increase())
    override fun decrease() = copy(quantity = quantity.decrease())
    override fun changeQuantity(newQuantity: Quantity) = copy(quantity = newQuantity)
    fun itemPrice() = product.price.multiply(quantity)
}

data object NoItem : Item

data class ItemId(val id: Identity) {
    companion object {
        fun isValid(value: String?) = !value.isNullOrBlank()
    }
}

class UnknownItemException(message : String) : Exception(message)
