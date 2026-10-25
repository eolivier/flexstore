package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.infra.spring.adapter.mapping.ProductMapper
import org.flexstore.infra.spring.adapter.mapping.toJsonProducts
import org.flexstore.infra.spring.adapter.rest.json.DraftJsonProduct
import org.flexstore.infra.spring.adapter.rest.json.EditedJsonProduct
import org.flexstore.infra.spring.adapter.rest.json.JsonProduct
import org.flexstore.infra.spring.metrics.ProductMetrics
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "Products")
@RestController
@RequestMapping("/api/products")
class ProductController(private val productUseCases: ProductUseCases,
                        private val mapper: ProductMapper,
                        private val productMetrics: ProductMetrics) {

    @Operation(summary = "Create a product")
    @ApiResponse(responseCode = "201", description = "Product created",
        content = [Content(schema = Schema(implementation = JsonProduct::class))]
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody draftJsonProduct: DraftJsonProduct): JsonProduct {
        val createProduct = productUseCases.createProduct(mapper.toDomain(draftJsonProduct))
        val toJson = mapper.toJson(createProduct)
        productMetrics.incrementCreated()
        return toJson
    }

    @Operation(summary = "Edit a product (name, description, category, price)")
    @ApiResponse(responseCode = "200", description = "The edited product",
        content = [Content(schema = Schema(implementation = JsonProduct::class))]
    )
    @ApiResponse(responseCode = "404", description = "Unknown product")
    @PutMapping("/{id}")
    fun update(@PathVariable id: String, @RequestBody editedJsonProduct: EditedJsonProduct): JsonProduct =
        mapper.toJson(productUseCases.updateProduct(mapper.toDomain(ProductId(Identity(id)), editedJsonProduct)))

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
        val jsonProducts = productUseCases.getProducts().toJsonProducts()
        // --- Observability metrics ---
        val businessDuration = System.nanoTime() - start
        productMetrics.recordList(businessDuration)
        productMetrics.setInventory(jsonProducts.size)
        // --------------------------------
        return jsonProducts
    }

    @Operation(
        summary = "Retrieve a product by ID",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved the product",
                content = [Content(schema = Schema(implementation = JsonProduct::class))]
            ),
            ApiResponse(responseCode = "404", description = "Product not found")
        ]
    )
    @GetMapping("/{id}")
    fun getProduct(@PathVariable id: String): JsonProduct =
        mapper.toJson(productUseCases.getProduct(ProductId(Identity(id))))
}
