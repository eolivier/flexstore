package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.cart.*
import org.flexstore.domain.cart.usecase.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
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
class CartController(
    private val cartUseCases: CartUseCases,
    private val actorResolutionService: ActorResolutionService
) {

    @Operation(summary = "Retrieve the items of the cart of the customer or of the visitor")
    @GetMapping("/cart-items")
    fun getCartItems(
        @AuthenticationPrincipal jwt: Jwt?,
        @RequestHeader(CART_ID_HEADER, required = false) cartId: String?
    ): JsonCartItems {
        val owner = customer(jwt) ?: visitor(cartId) ?: return JsonCartItems(emptyList(), BigDecimal.ZERO, "€")
        return cartUseCases.getCart(actorOf(jwt), owner).items.toJsonCartItems()
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
        val givenItemId = jsonItem.itemId.orEmpty()
        if (ItemId.isValid(givenItemId)) {
            val itemId = ItemId(Identity(givenItemId))
            cartUseCases.changeQuantity(ChangeCartItemQuantityRequest(actorOf(jwt), owner, itemId, quantity))
        } else {
            val productId = ProductId(Identity(jsonItem.productId))
            cartUseCases.addItem(AddItemToCartRequest(actorOf(jwt), owner, productId, quantity))
        }
        return JsonSaveItemResponse("ok", (owner as? CartOwner.Visitor)?.cartId?.id?.value)
    }

    @Operation(summary = "Merge the cart of the visitor (X-Cart-Id) into the cart of the signed-in customer")
    @PostMapping("/merge")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun mergeCarts(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestHeader(CART_ID_HEADER, required = false) cartId: String?
    ) {
        val visitor = visitor(cartId) ?: return
        cartUseCases.mergeCarts(MergeCartsRequest(visitor, actorOf(jwt)))
    }

    // The customer of the token, or a visitor when there is none
    private fun actorOf(jwt: Jwt?) =
        jwt?.let { actorResolutionService.actorOf(UserId.of(it.subject)) } ?: actorResolutionService.visitor()

    private fun customer(jwt: Jwt?) = jwt?.let { CartOwner.Customer(UserId.ValidUserId(it.subject)) }

    // An unknown or malformed id is ignored: the visitor gets a new cart
    private fun visitor(cartId: String?): CartOwner.Visitor? {
        val givenCartId = cartId.orEmpty()
        return if (VisitorCartId.isValid(givenCartId)) CartOwner.Visitor(VisitorCartId(Identity(givenCartId))) else null
    }
}
