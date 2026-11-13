package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.discount.PromoCode
import org.flexstore.domain.discount.usecase.DiscountCodeUseCases
import org.flexstore.infra.spring.adapter.rest.json.JsonDiscountCode
import org.flexstore.infra.spring.adapter.rest.json.JsonNewDiscountCode
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.of
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt

// The discount codes, given and deleted by the administrators (ADMIN role, see SecurityConfig, checked again by
// the use cases)
@Tag(name = "Administration")
@RestController
@RequestMapping("/api/admin/discount-codes")
class AdminDiscountCodeController(
    private val discountCodeUseCases: DiscountCodeUseCases,
    private val actorResolutionService: ActorResolutionService
) {

    @Operation(summary = "Retrieve the discount codes, newest first")
    @GetMapping
    fun getDiscountCodes(@AuthenticationPrincipal reader: Jwt): List<JsonDiscountCode> {
        val actor = actorResolutionService.actorOf(UserId.of(reader.subject))
        return discountCodeUseCases.getAll(actor).asList().map { JsonDiscountCode(it) }
    }

    @Operation(
        summary = "Give a discount code: a percentage or an amount off the items, until a date, for some orders"
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createDiscountCode(
        @AuthenticationPrincipal creator: Jwt,
        @RequestBody newDiscountCode: JsonNewDiscountCode
    ): JsonDiscountCode {
        val discountCode = newDiscountCode.toDiscountCode()
        val actor = actorResolutionService.actorOf(UserId.of(creator.subject))
        discountCodeUseCases.create(actor, discountCode)
        return JsonDiscountCode(discountCode)
    }

    @Operation(summary = "Delete a discount code: the orders that used it keep their discount")
    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteDiscountCode(@AuthenticationPrincipal editor: Jwt, @PathVariable code: String) {
        val actor = actorResolutionService.actorOf(UserId.of(editor.subject))
        discountCodeUseCases.delete(actor, PromoCode.from(code))
    }
}
