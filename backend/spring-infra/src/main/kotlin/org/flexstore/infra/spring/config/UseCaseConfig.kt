package org.flexstore.infra.spring.config

import org.flexstore.domain.repository.ProductRepository
import org.flexstore.domain.usecase.product.CreateProductUseCase
import org.flexstore.domain.usecase.product.ProductUseCaseFacade
import org.flexstore.domain.usecase.product.ReadProductUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class UseCaseConfig {

    @Bean
    open fun productUseCaseFacade(createProductUseCase: CreateProductUseCase,
                                 readProductUseCase: ReadProductUseCase): ProductUseCaseFacade {
        return ProductUseCaseFacade(createProductUseCase, readProductUseCase)
    }

    @Bean
    open fun createProductUseCase(productRepository: ProductRepository): CreateProductUseCase {
        return CreateProductUseCase(productRepository)
    }

    @Bean
    open fun readProductUseCase(productRepository: ProductRepository): ReadProductUseCase {
        return ReadProductUseCase(productRepository)
    }
}