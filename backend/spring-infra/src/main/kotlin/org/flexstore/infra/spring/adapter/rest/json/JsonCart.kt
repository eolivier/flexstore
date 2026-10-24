package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.valueobject.CartItem
import java.math.BigDecimal

// Only the item id, the product id and the quantity are used: the product is read from the catalog
data class JsonItem @JsonCreator constructor(
    @JsonProperty("itemId") val itemId: String? = "",
    @JsonProperty("productId") val productId: String,
    @JsonProperty("productName") val productName: String,
    @JsonProperty("productDescription") val productDescription: String,
    @JsonProperty("productCategory") val productCategory: String,
    @JsonProperty("productPrice") val productPrice: BigDecimal,
    @JsonProperty("productCurrency") val productCurrency: String,
    @JsonProperty("productQuantity") val productQuantity: Int
)

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