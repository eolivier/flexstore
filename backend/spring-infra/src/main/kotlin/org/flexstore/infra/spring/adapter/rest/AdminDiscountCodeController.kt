package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.discount.PromoCode
import org.flexstore.domain.discount.usecase.DiscountCodeUseCases
import org.flexstore.infra.spring.adapter.rest.json.JsonDiscountCode
import org.flexstore.infra.spring.adapter.rest.json.JsonNewDiscountCode
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt

// The discount codes, given and deleted by the administrators (ADMIN role, see SecurityConfig, checked again by
// the use cases)
@RestController
@RequestMapping("/api/admin/discount-codes")
class AdminDiscountCodeController(
    private val discountCodeUseCases: DiscountCodeUseCases,
    private val actorResolutionService: ActorResolutionService
) {

    @GetMapping
    fun getDiscountCodes(@AuthenticationPrincipal reader: Jwt): List<JsonDiscountCode> {
        val actor = actorResolutionService.actorOf(reader)
        return discountCodeUseCases.getAll(actor).asList().map { JsonDiscountCode(it) }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createDiscountCode(
        @AuthenticationPrincipal creator: Jwt,
        @RequestBody newDiscountCode: JsonNewDiscountCode
    ): JsonDiscountCode {
        val discountCode = newDiscountCode.toDiscountCode()
        val actor = actorResolutionService.actorOf(creator)
        discountCodeUseCases.create(actor, discountCode)
        return JsonDiscountCode(discountCode)
    }

    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteDiscountCode(@AuthenticationPrincipal editor: Jwt, @PathVariable code: String) {
        val actor = actorResolutionService.actorOf(editor)
        discountCodeUseCases.delete(actor, PromoCode.from(code))
    }
}
