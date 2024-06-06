package org.flexstore.domain

import java.math.BigDecimal

data class OneItem(val itemId: ItemId, val product: Product)
data class Item(val itemId: ItemId, val product: Product, val quantity: Quantity) {
    fun increase() = Item(itemId, product, quantity.increase())
    fun decrease() = Item(itemId, product, quantity.decrease())
    fun changeQuantity(newQuantity: Quantity) =
        Item(itemId, product, quantity.changeQuantity(newQuantity))
}
data object NoItem
data class ItemId(val id: Identity)
class UnknownItemException(message : String) : Exception(message)
data class Product(val productId: ProductId, val name: Name, val price: Price)
data class ProductId(val id: Identity)
data class Name(val value: String)
data class Price(val amount: Amount, val currency: Currency)
data class Amount(val value: BigDecimal)
data class Quantity(val value: Int) {
    fun increase() = Quantity(value + 1)
    fun decrease() = Quantity(value - 1)
    fun changeQuantity(newQuantity: Quantity) = Quantity(newQuantity.value)
}
data class Identity(val value: String)
enum class Currency { EUR, USD }
