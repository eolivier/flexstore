package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.loyalty.usecase.LoyaltyUseCases
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.of
import org.flexstore.infra.spring.adapter.rest.json.JsonLoyaltyStatement
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

// The loyalty programme of a signed-in customer: a line of points for each of their paid orders
@Tag(name = "Loyalty")
@RestController
@RequestMapping("/api/loyalty")
class LoyaltyController(
    private val loyaltyUseCases: LoyaltyUseCases,
    private val actorResolutionService: ActorResolutionService
) {

    @Operation(summary = "Retrieve the loyalty points of the signed-in customer")
    @GetMapping
    fun getPoints(@AuthenticationPrincipal jwt: Jwt): JsonLoyaltyStatement {
        val customer = actorResolutionService.actorOf(UserId.of(jwt.subject))
        return JsonLoyaltyStatement.of(loyaltyUseCases.getStatement(customer))
    }
}
