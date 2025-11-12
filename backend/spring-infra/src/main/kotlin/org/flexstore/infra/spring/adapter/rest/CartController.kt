package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.entity.Cart
import org.flexstore.domain.repository.ItemRepository
import org.flexstore.infra.spring.adapter.mapping.toJsonCartItems
import org.flexstore.infra.spring.adapter.rest.json.JsonItem
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Cart")
@RestController
@RequestMapping("/api/cart")
class CartController(itemRepository: ItemRepository) {

    private val cart = Cart(itemRepository)

    @Operation(summary = "Retrieve all cart items",
        description = "Returns the full list of items in the cart"
    )
    @GetMapping("/cart-items")
    fun getCartItems() = cart.getItems().toJsonCartItems()

    @Operation(summary = "Add an item to the cart")
    @PostMapping("/save")
    @ResponseStatus(HttpStatus.CREATED)
    fun addItem(@RequestBody jsonItem: JsonItem): ResponseEntity<Any> {
        cart.save(jsonItem.toItem())
        return ResponseEntity.ok(mapOf("status" to "ok"))
    }
}
