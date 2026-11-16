package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.product.model.ProductId
import org.flexstore.domain.sharedkernel.Identity
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
@RestController
@RequestMapping("/api/wishlist")
class WishlistController(
    private val wishlistUseCases: WishlistUseCases,
    private val actorResolutionService: ActorResolutionService
) {

    @GetMapping
    fun getWishlist(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestHeader(HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "en") acceptLanguage: String
    ): List<JsonProduct> {
        val wishlist = wishlistUseCases.getWishlist(actorResolutionService.actorOf(jwt))
        return wishlist.asList().toJsonProducts(languageAcceptedIn(acceptLanguage))
    }

    @PutMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun saveProduct(@AuthenticationPrincipal jwt: Jwt, @PathVariable productId: String) {
        wishlistUseCases.add(actorResolutionService.actorOf(jwt), ProductId(Identity(productId)))
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun removeProduct(@AuthenticationPrincipal jwt: Jwt, @PathVariable productId: String) {
        wishlistUseCases.remove(actorResolutionService.actorOf(jwt), ProductId(Identity(productId)))
    }
}
