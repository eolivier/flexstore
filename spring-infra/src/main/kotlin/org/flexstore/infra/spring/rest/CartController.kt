package org.flexstore.infra.spring.rest

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.entity.Cart
import org.flexstore.domain.repository.ItemRepository
import org.flexstore.domain.valueobject.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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
    fun addItem(@RequestBody jsonItem: JsonItem): ResponseEntity<Any> {
        cart.save(jsonItem.toItem())
        return ResponseEntity.ok(mapOf("status" to "ok"))
    }

    private fun <E> List<E>.toJsonCartItems() = this.map {
        when (it) {
            is CartItem -> JsonCartItem(
                it.itemId.id.value,
                it.product.productId.id.value,
                it.product.name.value,
                it.product.description.value,
                it.product.category.name,
                it.product.price.amount.value,
                it.product.price.currency.symbol,
                it.quantity.value,
                it.itemPrice().amount.value
            )
            else -> throw IllegalArgumentException("Unknown item type: ${it!!::class.java}")
        }
    }
}

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
        val product = Product(ProductId(Identity(productId)), Name(productName), Description(productDescription), Category.valueOf(productCategory), price)
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
)

data class JsonCartItems @JsonCreator constructor(
    @JsonProperty("items") val items: List<JsonCartItem>,
    @JsonProperty("total") val total: BigDecimal
)