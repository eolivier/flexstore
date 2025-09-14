package org.flexstore.infra.spring.rest

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.entity.Cart
import org.flexstore.domain.repository.ItemRepository
import org.flexstore.domain.valueobject.*
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.ucop.domain.entity.Name
import java.math.BigDecimal

@RestController
@RequestMapping("/api/cart")
class CartController(itemRepository: ItemRepository) {

    private val cart = Cart(itemRepository)

    @GetMapping("/items")
    fun getItems(): List<JsonItem> = cart.getItems().toJsonItems()

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    fun addItem(@RequestBody newJsonItemToAdd: JsonItemToAdd) = cart.addOrReplaceItem(newJsonItemToAdd.toItem())

    private fun <E> List<E>.toJsonItems() = this.map {
        when (it) {
            is OneItem -> JsonItem(it.itemId.id.value, it.product.name.value, it.quantity.value)
            else -> throw IllegalArgumentException("Unknown item type: ${it!!::class.java}")
        }
    }
}
data class JsonItem @JsonCreator constructor(
    @JsonProperty("itemId") val itemId: String,
    @JsonProperty("productName") val productName: String,
    @JsonProperty("productQuantity") val productQuantity: Int
)

data class JsonItemToAdd @JsonCreator constructor(
    @JsonProperty("itemId") val itemId: String,
    @JsonProperty("price") val price: BigDecimal,
    @JsonProperty("currency") val currency: Currency,
    @JsonProperty("productId") val productId: String,
    @JsonProperty("productName") val productName: String,
    @JsonProperty("productQuantity") val productQuantity: Int
) {
    fun toItem(): Item {
        val price = Price(Amount(price), currency)
        val product = Product(ProductId(Identity(productId)), Name(productName), price)
        return OneItem(ItemId(Identity(itemId)), product, Quantity(productQuantity))
    }

}