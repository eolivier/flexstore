package org.flexstore.infra.spring.rest

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.entity.Cart
import org.flexstore.domain.repository.ItemRepository
import org.flexstore.domain.valueobject.*
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/cart")
class CartController(itemRepository: ItemRepository) {

    private val cart = Cart(itemRepository)

    @GetMapping("/items")
    fun getItems(): List<JsonCartItem> = cart.getItems().toJsonCartItems()

    @PostMapping("/save")
    @ResponseStatus(HttpStatus.CREATED)
    fun addItem(@RequestBody jsonItem: JsonItem) = cart.save(jsonItem.toItem())

    private fun <E> List<E>.toJsonCartItems() = this.map {
        when (it) {
            is CartItem -> JsonCartItem(
                it.itemId.id.value,
                it.product.productId.id.value,
                it.product.name.value,
                it.product.description.value,
                it.product.price.amount.value,
                it.product.price.currency.symbol,
                it.quantity.value)
            else -> throw IllegalArgumentException("Unknown item type: ${it!!::class.java}")
        }
    }
}

data class JsonItem @JsonCreator constructor(
    @JsonProperty("itemId") val itemId: String? = "",
    @JsonProperty("productId") val productId: String,
    @JsonProperty("productName") val productName: String,
    @JsonProperty("productDescription") val productDescription: String,
    @JsonProperty("productPrice") val productPrice: BigDecimal,
    @JsonProperty("productCurrency") val productCurrency: String,
    @JsonProperty("productQuantity") val productQuantity: Int
) {
    fun toItem(): Item {
        val price = Price(Amount(productPrice), Currency.valueOf(productCurrency))
        val product = Product(ProductId(Identity(productId)), Name(productName), price)
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
    @JsonProperty("productPrice") val productPrice: BigDecimal,
    @JsonProperty("productCurrency") val productCurrency: String,
    @JsonProperty("productQuantity") val productQuantity: Int
)