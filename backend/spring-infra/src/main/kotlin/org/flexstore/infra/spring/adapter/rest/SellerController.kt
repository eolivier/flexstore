package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.seller.usecase.SellerUseCases
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.infra.spring.adapter.rest.json.JsonSeller
import org.springframework.web.bind.annotation.*

// The seller written on the invoices, for the administrators (ADMIN role, see SecurityConfig)
@Tag(name = "Administration")
@RestController
@RequestMapping("/api/admin/seller")
class SellerController(private val sellerUseCases: SellerUseCases) {

    @Operation(summary = "Retrieve the seller written on the next invoices")
    @GetMapping
    fun getSeller() = JsonSeller(sellerUseCases.currentSeller())

    @Operation(summary = "Change the seller written on the next invoices; the invoices issued keep theirs")
    @PutMapping
    fun updateSeller(@RequestBody seller: JsonSeller): JsonSeller {
        val updatedSeller = sellerUseCases.update(seller.toSellerDetails())
        return JsonSeller(updatedSeller)
    }
}
