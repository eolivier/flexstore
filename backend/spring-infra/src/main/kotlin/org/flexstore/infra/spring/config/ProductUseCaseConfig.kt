package org.flexstore.infra.spring.config

import org.flexstore.domain.product.*
import org.flexstore.domain.product.usecase.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The use cases of the product sub-domain, declared step by step: use cases, then their group
@Configuration
open class ProductUseCaseConfig {

    @Bean
    open fun createProductUseCase(productRepository: ProductRepository): CreateProductUseCase {
        return CreateProductUseCase(productRepository)
    }

    @Bean
    open fun updateProductUseCase(productRepository: ProductRepository): UpdateProductUseCase {
        return UpdateProductUseCase(productRepository)
    }

    @Bean
    open fun readProductUseCase(productRepository: ProductRepository): ReadProductUseCase {
        return ReadProductUseCase(productRepository)
    }

    @Bean
    open fun listProductsUseCase(productRepository: ProductRepository) = ListProductsUseCase(productRepository)

    @Bean
    open fun proposeSkuUseCase(productRepository: ProductRepository) = ProposeSkuUseCase(productRepository)

    @Bean
    open fun productUseCases(createProductUseCase: CreateProductUseCase,
                             readProductUseCase: ReadProductUseCase,
                             listProductsUseCase: ListProductsUseCase,
                             updateProductUseCase: UpdateProductUseCase,
                             proposeSkuUseCase: ProposeSkuUseCase) =
        ProductUseCases(
            createProductUseCase,
            readProductUseCase,
            listProductsUseCase,
            updateProductUseCase,
            proposeSkuUseCase
        )
}
