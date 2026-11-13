package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.product.usecase.*
import org.flexstore.domain.sharedkernel.*
import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.infra.spring.adapter.mapping.ProductMapper
import org.flexstore.infra.spring.adapter.mapping.languageAcceptedIn
import org.flexstore.infra.spring.adapter.mapping.toJsonProducts
import org.flexstore.infra.spring.adapter.rest.json.DraftJsonProduct
import org.flexstore.infra.spring.adapter.rest.json.EditedJsonProduct
import org.flexstore.infra.spring.adapter.rest.json.JsonProduct
import org.flexstore.infra.spring.metrics.ProductMetrics
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.of
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt

@Tag(name = "Products")
@RestController
@RequestMapping("/api/products")
class ProductController(private val productUseCases: ProductUseCases,
                        private val mapper: ProductMapper,
                        private val productMetrics: ProductMetrics,
                        private val actorResolutionService: ActorResolutionService) {

    @Operation(summary = "Create a product")
    @ApiResponse(responseCode = "201", description = "Product created",
        content = [Content(schema = Schema(implementation = JsonProduct::class))]
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @AuthenticationPrincipal creator: Jwt,
        @RequestBody draftJsonProduct: DraftJsonProduct
    ): JsonProduct {
        val draftProduct = mapper.toDomain(draftJsonProduct)
        val actor = actorResolutionService.actorOf(UserId.of(creator.subject))
        val createProduct = productUseCases.createProduct(actor, draftProduct)
        val toJson = mapper.toJson(createProduct)
        productMetrics.incrementCreated()
        return toJson
    }

    @Operation(summary = "Edit a product (translations, category, price)")
    @ApiResponse(responseCode = "200", description = "The edited product",
        content = [Content(schema = Schema(implementation = JsonProduct::class))]
    )
    @ApiResponse(responseCode = "404", description = "Unknown product")
    @PutMapping("/{id}")
    fun update(
        @AuthenticationPrincipal editor: Jwt,
        @PathVariable id: String,
        @RequestBody editedJsonProduct: EditedJsonProduct
    ): JsonProduct {
        val editedProduct = mapper.toDomain(ProductId(id), editedJsonProduct)
        val actor = actorResolutionService.actorOf(UserId.of(editor.subject))
        return mapper.toJson(productUseCases.updateProduct(actor, editedProduct))
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
    fun getProducts(
        @RequestHeader(HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "en") acceptLanguage: String
    ): List<JsonProduct> {
        val start = System.nanoTime()
        val products = productUseCases.getProducts(actorResolutionService.visitor())
        val jsonProducts = products.asList().toJsonProducts(languageAcceptedIn(acceptLanguage))
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
    fun getProduct(
        @PathVariable id: String,
        @RequestHeader(HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "en") acceptLanguage: String
    ): JsonProduct {
        val product = productUseCases.getProduct(actorResolutionService.visitor(), ProductId(id))
        return listOf(product).toJsonProducts(languageAcceptedIn(acceptLanguage)).single()
    }
}
