package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.cart.*
import org.flexstore.domain.cart.usecase.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
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
@RestController
@RequestMapping("/api/cart")
class CartController(
    private val cartUseCases: CartUseCases,
    private val actorResolutionService: ActorResolutionService
) {

    @GetMapping("/cart-items")
    fun getCartItems(
        @AuthenticationPrincipal jwt: Jwt?,
        @RequestHeader(CART_ID_HEADER, required = false) cartId: String?
    ): JsonCartItems {
        val owner = customer(jwt) ?: visitor(cartId) ?: return JsonCartItems(emptyList(), BigDecimal.ZERO, "€")
        return cartUseCases.getCart(actorResolutionService.actorOf(jwt), owner).items.toJsonCartItems()
    }

    @PostMapping("/save")
    fun addItem(
        @AuthenticationPrincipal jwt: Jwt?,
        @RequestHeader(CART_ID_HEADER, required = false) cartId: String?,
        @RequestBody jsonItem: JsonItem
    ): JsonSaveItemResponse {
        val owner = customer(jwt) ?: visitor(cartId) ?: CartOwner.Visitor(VisitorCartId.nextId())
        val quantity = Quantity(jsonItem.productQuantity)
        val givenItemId = jsonItem.itemId.orEmpty()
        val actor = actorResolutionService.actorOf(jwt)
        if (ItemId.isValid(givenItemId)) {
            val itemId = ItemId(Identity(givenItemId))
            cartUseCases.changeQuantity(ChangeCartItemQuantityRequest(actor, owner, itemId, quantity))
        } else {
            val productId = ProductId(Identity(jsonItem.productId))
            cartUseCases.addItem(AddItemToCartRequest(actor, owner, productId, quantity))
        }
        return JsonSaveItemResponse("ok", (owner as? CartOwner.Visitor)?.cartId?.id?.value)
    }

    @PostMapping("/merge")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun mergeCarts(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestHeader(CART_ID_HEADER, required = false) cartId: String?
    ) {
        val visitor = visitor(cartId) ?: return
        cartUseCases.mergeCarts(MergeCartsRequest(visitor, actorResolutionService.actorOf(jwt)))
    }

    private fun customer(jwt: Jwt?) = jwt?.let { CartOwner.Customer(UserId.ValidUserId(it.subject)) }

    // An unknown or malformed id is ignored: the visitor gets a new cart
    private fun visitor(cartId: String?): CartOwner.Visitor? {
        val givenCartId = cartId.orEmpty()
        return if (VisitorCartId.isValid(givenCartId)) CartOwner.Visitor(VisitorCartId(Identity(givenCartId))) else null
    }
}
