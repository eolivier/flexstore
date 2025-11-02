package org.flexstore.infra.spring.adapter.rest

import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.usecase.product.ProductUseCaseFacade
import org.flexstore.infra.spring.adapter.mapping.ProductMapper
import org.flexstore.infra.spring.adapter.mapping.toJsonProducts
import org.flexstore.infra.spring.adapter.rest.json.DraftJsonProduct
import org.flexstore.infra.spring.adapter.rest.json.JsonProduct
import org.flexstore.infra.spring.metrics.ProductMetrics
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "Products")
@RestController
@RequestMapping("/api/products")
class ProductController(private val productUseCaseFacade: ProductUseCaseFacade,
                        private val mapper: ProductMapper,
                        private val productMetrics: ProductMetrics) {

    @Operation(summary = "Create a product")
    @ApiResponse(responseCode = "201", description = "Product created",
        content = [Content(schema = Schema(implementation = JsonProduct::class))]
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody draftJsonProduct: DraftJsonProduct): JsonProduct {
        val createProduct = productUseCaseFacade.createProduct(mapper.toDomain(draftJsonProduct))
        val toJson = mapper.toJson(createProduct)
        productMetrics.incrementCreated()
        return toJson
    }

    @Operation(
        summary = "Retrieve all products",
        description = "Returns the full list of available products",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved the list of products",
                content = [
                    Content(
                        array = ArraySchema(
                            schema = Schema(implementation = JsonProduct::class)
                        )
                    )
                ]
            )
        ]
    )
    @Timed(
        value = "flexstore.products.list.timer",
        description = "Time taken to retrieve and map all products",
        percentiles = [0.5, 0.95, 0.99]
    )
    @GetMapping("/")
    fun getProducts(): List<JsonProduct> {
        val start = System.nanoTime()
        val jsonProducts = productUseCaseFacade.getProducts().toJsonProducts()
        // --- Observability metrics ---
        val businessDuration = System.nanoTime() - start
        productMetrics.recordList(businessDuration)
        productMetrics.setInventory(jsonProducts.size)
        // --------------------------------
        return jsonProducts
    }
}


