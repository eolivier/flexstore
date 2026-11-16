package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.product.usecase.*
import org.flexstore.domain.sharedkernel.*
import io.micrometer.core.annotation.Timed
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
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt

@RestController
@RequestMapping("/api/products")
class ProductController(private val productUseCases: ProductUseCases,
                        private val mapper: ProductMapper,
                        private val productMetrics: ProductMetrics,
                        private val actorResolutionService: ActorResolutionService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(
        @AuthenticationPrincipal creator: Jwt,
        @RequestBody draftJsonProduct: DraftJsonProduct
    ): JsonProduct {
        val draftProduct = mapper.toDomain(draftJsonProduct)
        val actor = actorResolutionService.actorOf(creator)
        val createProduct = productUseCases.createProduct(actor, draftProduct)
        val toJson = mapper.toJson(createProduct)
        productMetrics.incrementCreated()
        return toJson
    }

    @PutMapping("/{id}")
    fun update(
        @AuthenticationPrincipal editor: Jwt,
        @PathVariable id: String,
        @RequestBody editedJsonProduct: EditedJsonProduct
    ): JsonProduct {
        val editedProduct = mapper.toDomain(ProductId(Identity(id)), editedJsonProduct)
        val actor = actorResolutionService.actorOf(editor)
        return mapper.toJson(productUseCases.updateProduct(actor, editedProduct))
    }

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

    @GetMapping("/{id}")
    fun getProduct(
        @PathVariable id: String,
        @RequestHeader(HttpHeaders.ACCEPT_LANGUAGE, defaultValue = "en") acceptLanguage: String
    ): JsonProduct {
        val product = productUseCases.getProduct(actorResolutionService.visitor(), ProductId(Identity(id)))
        return listOf(product).toJsonProducts(languageAcceptedIn(acceptLanguage)).single()
    }
}
