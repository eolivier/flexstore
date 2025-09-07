package org.flexstore.infra.spring.rest

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.entity.*
import org.flexstore.domain.repository.ItemRepository
import org.flexstore.domain.valueobject.Item
import org.flexstore.domain.valueobject.NoItem
import org.flexstore.domain.valueobject.OneItem
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cart")
class CartController(itemRepository: ItemRepository) {

    private val cart = Cart(itemRepository)

    @GetMapping("/items")
    fun getItems(): List<JsonItem> = cart.getItems().toJsonItems()

    @PostMapping("/add")
    @ResponseStatus(HttpStatus.CREATED)
    fun addItem(@RequestBody newItem: Item) = cart.addOrReplaceItem(newItem)

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