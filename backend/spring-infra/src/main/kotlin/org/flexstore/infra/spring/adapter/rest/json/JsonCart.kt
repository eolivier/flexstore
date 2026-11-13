package org.flexstore.infra.spring.adapter.rest.json

import org.flexstore.domain.cart.*
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.infra.spring.adapter.mapping.runningPercentage
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

// productPrice: the price to pay for one product, the promotion included. regularPrice: the price without the
// promotion. promotionPercentage: the percentage off while a promotion is running, null otherwise
data class JsonCartItem @JsonCreator constructor(
    @JsonProperty("itemId") val itemId: String,
    @JsonProperty("productId") val productId: String,
    @JsonProperty("productName") val productName: String,
    @JsonProperty("productDescription") val productDescription: String,
    @JsonProperty("productCategory") val productCategory: String,
    @JsonProperty("productPrice") val productPrice: BigDecimal,
    @JsonProperty("productCurrency") val productCurrency: String,
    @JsonProperty("productQuantity") val productQuantity: Int,
    @JsonProperty("itemPrice") val itemPrice: BigDecimal,
    @JsonProperty("regularPrice") val regularPrice: BigDecimal,
    @JsonProperty("promotionPercentage") val promotionPercentage: BigDecimal?
) {
    constructor(cartItem: CartItem) : this(
        cartItem.itemId.identity.value,
        cartItem.product.productId.identity.value,
        cartItem.product.name.value,
        cartItem.product.description.value,
        cartItem.product.category.name,
        cartItem.product.currentPrice.amount.value,
        cartItem.product.currentPrice.currency.symbol,
        cartItem.quantity.value,
        cartItem.itemPrice().amount.value,
        cartItem.product.price.amount.value,
        cartItem.product.promotion.runningPercentage()
    )
}

data class JsonCartItems @JsonCreator constructor(
    @JsonProperty("items") val items: List<JsonCartItem>,
    @JsonProperty("totalItemsPrice") val totalItemsPrice: BigDecimal,
    @JsonProperty("itemsCurrency") val itemsCurrency: String
)

// cartId: the cart id of a visitor, to send in the X-Cart-Id header; null for a signed-in customer
data class JsonSaveItemResponse(
    @JsonProperty("status") val status: String,
    @JsonProperty("cartId") val cartId: String?
)
