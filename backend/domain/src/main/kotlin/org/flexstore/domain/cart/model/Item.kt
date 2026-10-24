package org.flexstore.domain.cart.model

import org.flexstore.domain.product.*
import org.flexstore.domain.shared.*
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
