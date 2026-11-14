package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.product.model.ProductId
import org.flexstore.domain.restock.usecase.RestockUseCases
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.of
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

// The products a signed-in customer waits for: they are notified by email as soon as one is back in stock
@Tag(name = "Restock subscriptions")
@RestController
@RequestMapping("/api/restock-subscriptions")
class RestockSubscriptionController(
    private val restockUseCases: RestockUseCases,
    private val actorResolutionService: ActorResolutionService
) {

    @Operation(summary = "Retrieve the ids of the products the signed-in customer waits for")
    @GetMapping
    fun getSubscribedProductIds(@AuthenticationPrincipal jwt: Jwt): List<String> {
        val restockSubscriptions = restockUseCases.getSubscriptions(customer(jwt))
        return restockSubscriptions.asList().map { it.productId.id.value }
    }

    @Operation(summary = "Ask to be notified by email when a product out of stock is back, once per product")
    @PutMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun subscribe(@AuthenticationPrincipal jwt: Jwt, @PathVariable productId: String) {
        restockUseCases.subscribe(customer(jwt), ProductId(Identity(productId)))
    }

    @Operation(summary = "Stop waiting for a product, so the signed-in customer is no longer notified of its return")
    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun unsubscribe(@AuthenticationPrincipal jwt: Jwt, @PathVariable productId: String) {
        restockUseCases.unsubscribe(customer(jwt), ProductId(Identity(productId)))
    }

    private fun customer(jwt: Jwt) = actorResolutionService.actorOf(UserId.of(jwt.subject))
}
