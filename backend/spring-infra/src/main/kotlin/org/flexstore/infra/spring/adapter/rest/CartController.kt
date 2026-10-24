package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.entity.CartOwner
import org.flexstore.domain.entity.UserId
import org.flexstore.domain.entity.VisitorCartId
import org.flexstore.domain.usecase.basket.AddItemToCartRequest
import org.flexstore.domain.usecase.basket.CartUseCaseFacade
import org.flexstore.domain.usecase.basket.ChangeCartItemQuantityRequest
import org.flexstore.domain.usecase.basket.MergeCartsRequest
import org.flexstore.domain.valueobject.Identity
import org.flexstore.domain.valueobject.ItemId
import org.flexstore.domain.valueobject.ProductId
import org.flexstore.domain.valueobject.Quantity
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
class CartController(private val cartUseCaseFacade: CartUseCaseFacade) {

    @Operation(summary = "Retrieve the items of the cart of the customer or of the visitor")
    @GetMapping("/cart-items")
    fun getCartItems(
        @AuthenticationPrincipal jwt: Jwt?,
        @RequestHeader(CART_ID_HEADER, required = false) cartId: String?
    ): JsonCartItems {
        val owner = customer(jwt) ?: visitor(cartId) ?: return JsonCartItems(emptyList(), BigDecimal.ZERO, "€")
        return cartUseCaseFacade.getCart(owner).toItems().toJsonCartItems()
    }

    @Operation(summary = "Add an item to the cart, or change the quantity of an item (0 removes it)",
        description = "For a visitor, the response gives the cart id to send in the X-Cart-Id header")
    @PostMapping("/save")
    fun addItem(
        @AuthenticationPrincipal jwt: Jwt?,
        @RequestHeader(CART_ID_HEADER, required = false) cartId: String?,
        @RequestBody jsonItem: JsonItem
    ): JsonSaveItemResponse {
        val owner = customer(jwt) ?: visitor(cartId) ?: CartOwner.Visitor(VisitorCartId.generate())
        val quantity = Quantity(jsonItem.productQuantity)
        if (ItemId.isValid(jsonItem.itemId)) {
            cartUseCaseFacade.changeQuantity(ChangeCartItemQuantityRequest(owner, ItemId(Identity(jsonItem.itemId!!)), quantity))
        } else {
            cartUseCaseFacade.addItem(AddItemToCartRequest(owner, ProductId(Identity(jsonItem.productId)), quantity))
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
        cartUseCaseFacade.mergeCarts(MergeCartsRequest(visitor, customer(jwt)!!))
    }

    private fun customer(jwt: Jwt?) = jwt?.let { CartOwner.Customer(UserId.ValidUserId(it.subject)) }

    // An unknown or malformed id is ignored: the visitor gets a new cart
    private fun visitor(cartId: String?) =
        if (VisitorCartId.isValid(cartId)) CartOwner.Visitor(VisitorCartId(cartId!!)) else null
}
