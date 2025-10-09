package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.usecase.product.CreateProductUseCase
import org.flexstore.domain.usecase.product.ReadProductUseCase
import org.flexstore.infra.spring.adapter.mapping.ProductMapper
import org.flexstore.infra.spring.adapter.mapping.toJsonProducts
import org.flexstore.infra.spring.adapter.rest.json.DraftJsonProduct
import org.flexstore.infra.spring.adapter.rest.json.JsonProduct
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/products")
class ProductController(private val readProductUseCase: ReadProductUseCase,
                        private val createProductUseCase: CreateProductUseCase,
                        private val mapper: ProductMapper) {

    @PostMapping
    fun create(@RequestBody draftJsonProduct: DraftJsonProduct): JsonProduct {
        createProductUseCase.unfold(mapper.toDomain(draftJsonProduct))
        return mapper.toJson(createProductUseCase.createdProduct)
    }

    @GetMapping("/")
    fun getProducts(): List<JsonProduct> = readProductUseCase.getProducts().toJsonProducts()
}

