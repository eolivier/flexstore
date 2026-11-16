package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.product.model.ProductId
import org.flexstore.domain.restock.usecase.RestockUseCases
import org.flexstore.domain.sharedkernel.Identity
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

// The products a signed-in customer waits for: they are notified by email as soon as one is back in stock
@RestController
@RequestMapping("/api/restock-subscriptions")
class RestockSubscriptionController(
    private val restockUseCases: RestockUseCases,
    private val actorResolutionService: ActorResolutionService
) {

    @GetMapping
    fun getSubscribedProductIds(@AuthenticationPrincipal jwt: Jwt): List<String> {
        val restockSubscriptions = restockUseCases.getSubscriptions(actorResolutionService.actorOf(jwt))
        return restockSubscriptions.asList().map { it.productId.id.value }
    }

    @PutMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun subscribe(@AuthenticationPrincipal jwt: Jwt, @PathVariable productId: String) {
        restockUseCases.subscribe(actorResolutionService.actorOf(jwt), ProductId(Identity(productId)))
    }

    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun unsubscribe(@AuthenticationPrincipal jwt: Jwt, @PathVariable productId: String) {
        restockUseCases.unsubscribe(actorResolutionService.actorOf(jwt), ProductId(Identity(productId)))
    }
}
