package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.usecase.product.CreateProductUseCase
import org.flexstore.domain.usecase.product.ReadProductUseCase
import org.flexstore.infra.spring.adapter.mapping.ProductMapper
import org.flexstore.infra.spring.adapter.mapping.toJsonProducts
import org.flexstore.infra.spring.adapter.rest.json.DraftJsonProduct
import org.flexstore.infra.spring.adapter.rest.json.JsonProduct
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "Products")
@RestController
@RequestMapping("/api/products")
class ProductController(private val readProductUseCase: ReadProductUseCase,
                        private val createProductUseCase: CreateProductUseCase,
                        private val mapper: ProductMapper) {

    @Operation(summary = "Create a product")
    @ApiResponse(responseCode = "201", description = "Product created",
        content = [Content(schema = Schema(implementation = JsonProduct::class))]
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@RequestBody draftJsonProduct: DraftJsonProduct): JsonProduct {
        createProductUseCase.unfold(mapper.toDomain(draftJsonProduct))
        val toJson = mapper.toJson(createProductUseCase.createdProduct)
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
    @GetMapping("/")
    fun getProducts(): List<JsonProduct> = readProductUseCase.getProducts().toJsonProducts()
}

