package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.discount.PromoCode
import org.flexstore.domain.discount.usecase.DiscountCodeUseCases
import org.flexstore.infra.spring.adapter.rest.json.JsonDiscountCode
import org.flexstore.infra.spring.adapter.rest.json.JsonNewDiscountCode
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

// The discount codes, given and deleted by the administrators (ADMIN role, see SecurityConfig)
@Tag(name = "Administration")
@RestController
@RequestMapping("/api/admin/discount-codes")
class AdminDiscountCodeController(private val discountCodeUseCases: DiscountCodeUseCases) {

    @Operation(summary = "Retrieve the discount codes, newest first")
    @GetMapping
    fun getDiscountCodes(): List<JsonDiscountCode> =
        discountCodeUseCases.getAll().asList().map { JsonDiscountCode(it) }

    @Operation(summary = "Give a discount code: a percentage or an amount off the items, until a date, for some orders")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createDiscountCode(@RequestBody newDiscountCode: JsonNewDiscountCode): JsonDiscountCode {
        val discountCode = newDiscountCode.toDiscountCode()
        discountCodeUseCases.create(discountCode)
        return JsonDiscountCode(discountCode)
    }

    @Operation(summary = "Delete a discount code: the orders that used it keep their discount")
    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteDiscountCode(@PathVariable code: String) = discountCodeUseCases.delete(PromoCode.from(code))
}
