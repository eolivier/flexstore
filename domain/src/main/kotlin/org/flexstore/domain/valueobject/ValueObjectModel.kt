package org.flexstore.domain.valueobject

import java.math.BigDecimal

sealed interface Item {
    fun increase(): Item = this
    fun decrease(): Item = this
    fun changeQuantity(newQuantity: Quantity): Item = this
}

data class OneItem(
    val itemId: ItemId,
    val product: Product,
    val quantity: Quantity
) : Item {
    override fun increase() = copy(quantity = quantity.increase())
    override fun decrease() = copy(quantity = quantity.decrease())
    override fun changeQuantity(newQuantity: Quantity) = copy(quantity = newQuantity)
}
data object NoItem : Item

data class ItemId(val id: Identity)
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
data class Price(val amount: Amount, val currency: Currency)
data class Amount(val value: BigDecimal)
data class Quantity(val value: Int) {
    fun increase() = Quantity(value + 1)
    fun decrease() = Quantity(value - 1)
    fun changeQuantity(newQuantity: Quantity) = Quantity(newQuantity.value)
    fun isNegative() = value < 0
    fun isZero() = value == 0
    fun isNotZero() = !isZero()
}
data class Identity(val value: String)
enum class Currency { EUR, USD }

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