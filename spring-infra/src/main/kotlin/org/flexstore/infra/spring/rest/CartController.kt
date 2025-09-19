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

    @GetMapping("/cart-items")
    fun getCartItems() = cart.getItems().toJsonCartItems()

    @PostMapping("/save")
    @ResponseStatus(HttpStatus.CREATED)
    fun addItem(@RequestBody jsonItem: JsonItem): ResponseEntity<Any> {
        cart.save(jsonItem.toItem())
        return ResponseEntity.ok(mapOf("status" to "ok"))
    }

    private fun Items.toJsonCartItems() = JsonCartItems(
        this.items.map {
            when (it) {
                is CartItem -> JsonCartItem(it)
                else -> throw IllegalArgumentException("Unknown item type: ${it::class.java}")
            }
        },
        this.totalPrice().amount.value,
        this.currency().symbol
    )
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
        val productId = ProductId(Identity(productId))
        val name = Name(productName)
        val description = Description(productDescription)
        val category = Category.valueOf(productCategory)
        val product = Product(productId, name, description, category, price)
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