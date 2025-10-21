package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.valueobject.*
import java.math.BigDecimal

data class JsonItem @JsonCreator constructor(
    @JsonProperty("itemId") val itemId: String? = "",
    @JsonProperty("productId") val productId: String,
    @JsonProperty("productName") val productName: String,
    @JsonProperty("productDescription") val productDescription: String,
    @JsonProperty("productCategory") val productCategory: String,
    @JsonProperty("productPrice") val productPrice: BigDecimal,
    @JsonProperty("productCurrency") val productCurrency: String,
    @JsonProperty("productQuantity") val productQuantity: Int
) {
    fun toItem(): Item {
        val price = Price(Amount(productPrice), Currency.fromSymbol(productCurrency))
        val productId = ProductId(Identity(productId))
        val name = Name(productName)
        val description = Description(productDescription)
        val category = Category.valueOf(productCategory)
        val product = DefinedProduct(productId, name, description, category, price)
        if (ItemId.isValid(itemId)) {
            return CartItem(ItemId(Identity(itemId!!)), product, Quantity(productQuantity))
        }
        return DraftItem(product, Quantity(productQuantity))
    }
}

data class JsonCartItem @JsonCreator constructor(
    @JsonProperty("itemId") val itemId: String,
    @JsonProperty("productId") val productId: String,
    @JsonProperty("productName") val productName: String,
    @JsonProperty("productDescription") val productDescription: String,
    @JsonProperty("productCategory") val productCategory: String,
    @JsonProperty("productPrice") val productPrice: BigDecimal,
    @JsonProperty("productCurrency") val productCurrency: String,
    @JsonProperty("productQuantity") val productQuantity: Int,
    @JsonProperty("itemPrice") val itemPrice: BigDecimal
) {
    constructor(cartItem: CartItem) : this(
        cartItem.itemId.id.value,
        cartItem.product.productId.id.value,
        cartItem.product.name.value,
        cartItem.product.description.value,
        cartItem.product.category.name,
        cartItem.product.price.amount.value,
        cartItem.product.price.currency.symbol,
        cartItem.quantity.value,
        cartItem.itemPrice().amount.value
    )
}

data class JsonCartItems @JsonCreator constructor(
    @JsonProperty("items") val items: List<JsonCartItem>,
    @JsonProperty("totalItemsPrice") val totalItemsPrice: BigDecimal,
    @JsonProperty("itemsCurrency") val itemsCurrency: String
)