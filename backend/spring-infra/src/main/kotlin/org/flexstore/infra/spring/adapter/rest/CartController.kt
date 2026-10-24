package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.usecase.basket.AddItemToCartRequest
import org.flexstore.domain.usecase.basket.CartUseCaseFacade
import org.flexstore.domain.usecase.basket.ChangeCartItemQuantityRequest
import org.flexstore.domain.valueobject.Identity
import org.flexstore.domain.valueobject.ItemId
import org.flexstore.domain.valueobject.ProductId
import org.flexstore.domain.valueobject.Quantity
import org.flexstore.infra.spring.adapter.mapping.toJsonCartItems
import org.flexstore.infra.spring.adapter.rest.json.JsonItem
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Cart")
@RestController
@RequestMapping("/api/cart")
class CartController(private val cartUseCaseFacade: CartUseCaseFacade) {

    @Operation(summary = "Retrieve all cart items",
        description = "Returns the full list of items in the cart"
    )
    @GetMapping("/cart-items")
    fun getCartItems() = cartUseCaseFacade.getCart().toItems().toJsonCartItems()

    @Operation(summary = "Add an item to the cart, or change the quantity of an item (0 removes it)")
    @PostMapping("/save")
    @ResponseStatus(HttpStatus.CREATED)
    fun addItem(@RequestBody jsonItem: JsonItem): ResponseEntity<Any> {
        val quantity = Quantity(jsonItem.productQuantity)
        if (ItemId.isValid(jsonItem.itemId)) {
            cartUseCaseFacade.changeQuantity(ChangeCartItemQuantityRequest(ItemId(Identity(jsonItem.itemId!!)), quantity))
        } else {
            cartUseCaseFacade.addItem(AddItemToCartRequest(ProductId(Identity(jsonItem.productId)), quantity))
        }
        return ResponseEntity.ok(mapOf("status" to "ok"))
    }
}
