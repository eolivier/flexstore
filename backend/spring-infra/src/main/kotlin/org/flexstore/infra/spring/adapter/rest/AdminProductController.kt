package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.Identity
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.infra.spring.adapter.mapping.ProductMapper
import org.flexstore.infra.spring.adapter.rest.json.JsonAdminProduct
import org.flexstore.infra.spring.adapter.rest.json.JsonSkuProposal
import org.springframework.web.bind.annotation.*

// The products as the administrators edit them (ADMIN role, see SecurityConfig): with every translation, where
// the catalog gives each customer their language only
@Tag(name = "Administration")
@RestController
@RequestMapping("/api/admin/products")
class AdminProductController(private val productUseCases: ProductUseCases, private val mapper: ProductMapper) {

    @Operation(summary = "Retrieve a product with its translations in every language, to edit it")
    @GetMapping("/{id}")
    fun getProduct(@PathVariable id: String): JsonAdminProduct =
        mapper.toAdminJson(productUseCases.getProduct(ProductId(Identity(id))))

    @Operation(summary = "Propose a SKU free in the catalog, from the category and the name of a product")
    @GetMapping("/sku-proposal")
    fun proposeSku(
        @RequestParam category: String,
        @RequestParam(defaultValue = "") name: String
    ): JsonSkuProposal {
        val skuBasis = SkuBasis(Category.valueOf(category), Words.of(name))
        return JsonSkuProposal(productUseCases.proposeSkuFor(skuBasis).value)
    }
}
