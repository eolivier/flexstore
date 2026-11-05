package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.product.model.ProductId
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.wishlist.usecase.WishlistUseCases
import org.flexstore.infra.spring.adapter.mapping.languageAcceptedIn
import org.flexstore.infra.spring.adapter.mapping.toJsonProducts
import org.flexstore.infra.spring.adapter.rest.json.JsonProduct
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

// The wishlist of a signed-in customer: the products they saved, to buy them back later
@Tag(name = "Wishlist")
@RestController
@RequestMapping("/api/wishlist")
class WishlistController(private val wishlistUseCases: WishlistUseCases) {

    @Operation(summary = "Retrieve the products the signed-in customer saved to their wishlist, newest first")
    @GetMapping
    fun getWishlist(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestHeader(HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "en") acceptLanguage: String
    ): List<JsonProduct> {
        val wishlist = wishlistUseCases.getWishlist(UserId.ValidUserId(jwt.subject))
        return wishlist.asList().toJsonProducts(languageAcceptedIn(acceptLanguage))
    }

    @Operation(summary = "Save a product to the wishlist of the signed-in customer, once per product")
    @PutMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun saveProduct(@AuthenticationPrincipal jwt: Jwt, @PathVariable productId: String) {
        wishlistUseCases.add(UserId.ValidUserId(jwt.subject), ProductId(Identity(productId)))
    }

    @Operation(summary = "Remove a product from the wishlist of the signed-in customer")
    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeProduct(@AuthenticationPrincipal jwt: Jwt, @PathVariable productId: String) {
        wishlistUseCases.remove(UserId.ValidUserId(jwt.subject), ProductId(Identity(productId)))
    }
}
