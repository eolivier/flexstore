package org.flexstore.infra.spring.config

import org.flexstore.domain.image.*
import org.flexstore.domain.image.usecase.*
import org.flexstore.domain.product.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The use cases of the product image sub-domain, declared step by step: use cases, then their group
@Configuration
open class ProductImageUseCaseConfig {

    @Bean
    open fun addProductImageUseCase(productImageRepository: ProductImageRepository,
                                    productRepository: ProductRepository,
                                    imageResizerRepository: ImageResizerRepository) =
        AddProductImageUseCase(productImageRepository, productRepository, imageResizerRepository)

    @Bean
    open fun removeProductImageUseCase(productImageRepository: ProductImageRepository) =
        RemoveProductImageUseCase(productImageRepository)

    @Bean
    open fun readProductImageUseCase(productImageRepository: ProductImageRepository) =
        ReadProductImageUseCase(productImageRepository)

    @Bean
    open fun listProductsWithImageUseCase(productImageRepository: ProductImageRepository) =
        ListProductsWithImageUseCase(productImageRepository)

    @Bean
    open fun productImageUseCases(addProductImageUseCase: AddProductImageUseCase,
                                  removeProductImageUseCase: RemoveProductImageUseCase,
                                  readProductImageUseCase: ReadProductImageUseCase,
                                  listProductsWithImageUseCase: ListProductsWithImageUseCase) =
        ProductImageUseCases(
            addProductImageUseCase,
            removeProductImageUseCase,
            readProductImageUseCase,
            listProductsWithImageUseCase
        )
}
