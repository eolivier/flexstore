package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.seller.SellerSetupService
import org.flexstore.domain.seller.usecase.SellerUseCases
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.infra.spring.adapter.rest.json.JsonSeller
import org.flexstore.infra.spring.adapter.rest.json.JsonSocialNetworks
import org.springframework.web.bind.annotation.*
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.of
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt

// The accounts of the shop on the social networks, read by everyone (its footer shows them)
@Tag(name = "Seller")
@RestController
@RequestMapping("/api/social-networks")
class SocialNetworksController(private val sellerUseCases: SellerUseCases) {

    @Operation(summary = "Retrieve the accounts of the shop on the social networks; empty where it has none")
    @GetMapping
    fun getSocialNetworks() = JsonSocialNetworks(sellerUseCases.socialNetworks())
}

// The seller written on the invoices, for the administrators (ADMIN role, see SecurityConfig, checked again by
// the use cases)
@Tag(name = "Administration")
@RestController
@RequestMapping("/api/admin/seller")
class SellerController(
    private val sellerUseCases: SellerUseCases,
    private val sellerSetupService: SellerSetupService
) {

    @Operation(summary = "Finish the setup assistant: save the seller, then turn the seller setup of the settings on")
    @PutMapping("/setup")
    fun completeSetup(@AuthenticationPrincipal editor: Jwt, @RequestBody seller: JsonSeller): JsonSeller {
        val completedSeller = sellerSetupService.complete(UserId.of(editor.subject), seller.toSellerDetail())
        return JsonSeller(completedSeller)
    }


    @Operation(summary = "Retrieve the seller written on the next invoices")
    @GetMapping
    fun getSeller(@AuthenticationPrincipal reader: Jwt) =
        JsonSeller(sellerUseCases.currentSeller(UserId.of(reader.subject)))

    @Operation(summary = "Change the seller written on the next invoices; the invoices issued keep theirs")
    @PutMapping
    fun updateSeller(@AuthenticationPrincipal editor: Jwt, @RequestBody seller: JsonSeller): JsonSeller {
        val updatedSeller = sellerUseCases.update(UserId.of(editor.subject), seller.toSellerDetail())
        return JsonSeller(updatedSeller)
    }
}
