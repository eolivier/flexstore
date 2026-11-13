package org.flexstore.infra.spring.config

import org.flexstore.domain.discount.*
import org.flexstore.domain.discount.usecase.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The use cases of the discount code sub-domain, declared step by step: use cases, then their group
@Configuration
open class DiscountCodeUseCaseConfig {

    @Bean
    open fun createDiscountCodeUseCase(discountCodeRepository: DiscountCodeRepository) =
        CreateDiscountCodeUseCase(discountCodeRepository)

    @Bean
    open fun listDiscountCodesUseCase(discountCodeRepository: DiscountCodeRepository) =
        ListDiscountCodesUseCase(discountCodeRepository)

    @Bean
    open fun deleteDiscountCodeUseCase(discountCodeRepository: DiscountCodeRepository) =
        DeleteDiscountCodeUseCase(discountCodeRepository)

    @Bean
    open fun readUsableDiscountCodeUseCase(discountCodeRepository: DiscountCodeRepository) =
        ReadUsableDiscountCodeUseCase(discountCodeRepository)

    @Bean
    open fun useDiscountCodeUseCase(discountCodeRepository: DiscountCodeRepository) =
        UseDiscountCodeUseCase(discountCodeRepository)

    @Bean
    open fun discountCodeUseCases(createDiscountCodeUseCase: CreateDiscountCodeUseCase,
                                  listDiscountCodesUseCase: ListDiscountCodesUseCase,
                                  deleteDiscountCodeUseCase: DeleteDiscountCodeUseCase) =
        DiscountCodeUseCases(createDiscountCodeUseCase, listDiscountCodesUseCase, deleteDiscountCodeUseCase)

    @Bean
    open fun checkoutDiscountUseCases(readUsableDiscountCodeUseCase: ReadUsableDiscountCodeUseCase,
                                      useDiscountCodeUseCase: UseDiscountCodeUseCase) =
        CheckoutDiscountUseCases(readUsableDiscountCodeUseCase, useDiscountCodeUseCase)
}
