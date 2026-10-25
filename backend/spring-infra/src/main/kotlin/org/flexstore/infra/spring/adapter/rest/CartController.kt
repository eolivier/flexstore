package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.cart.*
import org.flexstore.domain.cart.usecase.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.infra.spring.adapter.mapping.toJsonCartItems
import org.flexstore.infra.spring.adapter.rest.json.JsonCartItems
import org.flexstore.infra.spring.adapter.rest.json.JsonItem
import org.flexstore.infra.spring.adapter.rest.json.JsonSaveItemResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

const val CART_ID_HEADER = "X-Cart-Id"

/**
 * The cart of a signed-in customer is found with the token; a visitor sends the id of their cart in the
 * X-Cart-Id header (the id is created on the first item and returned by POST /save).
 */
@Tag(name = "Cart")
@RestController
@RequestMapping("/api/cart")
class CartController(private val cartUseCases: CartUseCases) {

    @Operation(summary = "Retrieve the items of the cart of the customer or of the visitor")
    @GetMapping("/cart-items")
    fun getCartItems(
        @AuthenticationPrincipal jwt: Jwt?,
        @RequestHeader(CART_ID_HEADER, required = false) cartId: String?
    ): JsonCartItems {
        val owner = customer(jwt) ?: visitor(cartId) ?: return JsonCartItems(emptyList(), BigDecimal.ZERO, "€")
        return cartUseCases.getCart(owner).items.toJsonCartItems()
    }

    @Operation(summary = "Add an item to the cart, or change the quantity of an item (0 removes it)",
        description = "For a visitor, the response gives the cart id to send in the X-Cart-Id header")
    @PostMapping("/save")
    fun addItem(
        @AuthenticationPrincipal jwt: Jwt?,
        @RequestHeader(CART_ID_HEADER, required = false) cartId: String?,
        @RequestBody jsonItem: JsonItem
    ): JsonSaveItemResponse {
        val owner = customer(jwt) ?: visitor(cartId) ?: CartOwner.Visitor(VisitorCartId.nextId())
        val quantity = Quantity(jsonItem.productQuantity)
        if (ItemId.isValid(jsonItem.itemId)) {
            val itemId = ItemId(Identity(jsonItem.itemId!!))
            cartUseCases.changeQuantity(ChangeCartItemQuantityRequest(owner, itemId, quantity))
        } else {
            cartUseCases.addItem(AddItemToCartRequest(owner, ProductId(Identity(jsonItem.productId)), quantity))
        }
        return JsonSaveItemResponse("ok", (owner as? CartOwner.Visitor)?.cartId?.value)
    }

    @Operation(summary = "Merge the cart of the visitor (X-Cart-Id) into the cart of the signed-in customer")
    @PostMapping("/merge")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun mergeCarts(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestHeader(CART_ID_HEADER, required = false) cartId: String?
    ) {
        val visitor = visitor(cartId) ?: return
        cartUseCases.mergeCarts(MergeCartsRequest(visitor, customer(jwt)!!))
    }

    private fun customer(jwt: Jwt?) = jwt?.let { CartOwner.Customer(UserId.ValidUserId(it.subject)) }

    // An unknown or malformed id is ignored: the visitor gets a new cart
    private fun visitor(cartId: String?) =
        if (VisitorCartId.isValid(cartId)) CartOwner.Visitor(VisitorCartId(cartId!!)) else null
}
