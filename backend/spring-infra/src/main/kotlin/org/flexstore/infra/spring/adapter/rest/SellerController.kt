package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.seller.SellerSetupService
import org.flexstore.domain.seller.usecase.SellerUseCases
import org.flexstore.infra.spring.adapter.rest.json.JsonSeller
import org.flexstore.infra.spring.adapter.rest.json.JsonSocialNetworks
import org.springframework.web.bind.annotation.*
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt

// The accounts of the shop on the social networks, read by everyone (its footer shows them)
@RestController
@RequestMapping("/api/social-networks")
class SocialNetworksController(
    private val sellerUseCases: SellerUseCases,
    private val actorResolutionService: ActorResolutionService
) {

    @GetMapping
    fun getSocialNetworks() = JsonSocialNetworks(sellerUseCases.socialNetworks(actorResolutionService.visitor()))
}

// The seller written on the invoices, for the administrators (ADMIN role, see SecurityConfig, checked again by
// the use cases)
@RestController
@RequestMapping("/api/admin/seller")
class SellerController(
    private val sellerUseCases: SellerUseCases,
    private val sellerSetupService: SellerSetupService,
    private val actorResolutionService: ActorResolutionService
) {

    @PutMapping("/setup")
    fun completeSetup(@AuthenticationPrincipal editor: Jwt, @RequestBody seller: JsonSeller): JsonSeller {
        val actor = actorResolutionService.actorOf(editor)
        val completedSeller = sellerSetupService.complete(actor, seller.toSellerDetail())
        return JsonSeller(completedSeller)
    }

    @GetMapping
    fun getSeller(@AuthenticationPrincipal reader: Jwt): JsonSeller {
        val actor = actorResolutionService.actorOf(reader)
        return JsonSeller(sellerUseCases.currentSeller(actor))
    }

    @PutMapping
    fun updateSeller(@AuthenticationPrincipal editor: Jwt, @RequestBody seller: JsonSeller): JsonSeller {
        val actor = actorResolutionService.actorOf(editor)
        val updatedSeller = sellerUseCases.update(actor, seller.toSellerDetail())
        return JsonSeller(updatedSeller)
    }
}
