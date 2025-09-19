package org.flexstore.domain.valueobject

import java.math.BigDecimal

data class Items(val items: List<Item>) {
    fun totalPrice(): Price {

        val totalAmount = items.fold(BigDecimal.ZERO) { acc, item ->
            when (item) {
                is CartItem -> acc.add(item.itemPrice().amount.value)
                is DraftItem -> acc.add(item.itemPrice().amount.value)
                else -> acc
            }
        }
        return Price(Amount(totalAmount), currency())
    }
    fun currency(): Currency {
        return if (items.isNotEmpty()) {
            when (val firstItem = items.first()) {
                is CartItem -> firstItem.product.price.currency
                is DraftItem -> firstItem.product.price.currency
                else -> Currency.EUR
            }
        } else {
            Currency.EUR
        }
    }
}

sealed interface Item {
    fun increase(): Item = this
    fun decrease(): Item = this
    fun changeQuantity(newQuantity: Quantity): Item = this
}

data class CartItem(
    val itemId: ItemId,
    val product: Product,
    val quantity: Quantity,
) : Item {
    override fun increase() = copy(quantity = quantity.increase())
    override fun decrease() = copy(quantity = quantity.decrease())
    override fun changeQuantity(newQuantity: Quantity) = copy(quantity = newQuantity)
    fun itemPrice() = product.price.multiply(quantity)
}
data class DraftItem(
    val product: Product,
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
data class Product(
    val productId: ProductId,
    val name: Name,
    val description: Description,
    val category: Category,
    val price: Price) {
    constructor(productId: ProductId, name: Name, price: Price) : this(productId, name, Description("No description"), Category.NOT_DEFINED, price)
}
data class ProductId(val id: Identity)
data class Price(val amount: Amount, val currency: Currency) {
    fun multiply(quantity: Quantity) = Price(amount.multiply(quantity), currency)
}
data class Amount(val value: BigDecimal) {
    fun multiply(quantity: Quantity) = Amount(this.value.multiply(BigDecimal(quantity.value)))
}
data class Quantity(val value: Int) {
    fun addNewQuantity(newQuantity: Quantity) = Quantity(this.value + newQuantity.value)
    fun increase() = Quantity(value + 1)
    fun decrease() = Quantity(value - 1)
    fun changeQuantity(newQuantity: Quantity) = Quantity(newQuantity.value)
    fun isNegative() = value < 0
    fun isZero() = value == 0
    fun isNotZero() = !isZero()
}
data class Identity(val value: String)
enum class Currency(val symbol: String) {
    EUR("€"), USD("$");
    companion object {
        fun fromSymbol(symbol: String): Currency =
            entries.find { it.symbol == symbol }
                ?: throw IllegalArgumentException("Unknown currency for symbol : $symbol")
    }
}

data class Name(val value: String) {
    init {
        require(value.isNotBlank()) { "Name must not ne empty nor null" }
    }
}

data class Description(val value: String) {
    init {
        require(value.isNotBlank()) { "Description must not ne empty nor null" }
    }
}

enum class Category { ELECTRONICS, BOOKS, CLOTHING, HOME, BEAUTY, TOYS, SPORTS, AUTOMOTIVE, GROCERY, HEALTH, ACCESSORIES, NOT_DEFINED }