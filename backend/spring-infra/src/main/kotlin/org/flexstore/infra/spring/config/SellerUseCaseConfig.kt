package org.flexstore.infra.spring.config

import org.flexstore.domain.seller.*
import org.flexstore.domain.seller.usecase.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The use cases of the seller sub-domain, declared step by step: use cases, then their group
@Configuration
open class SellerUseCaseConfig {

    @Bean
    open fun readSellerUseCase(sellerRepository: SellerRepository) = ReadSellerUseCase(sellerRepository)

    @Bean
    open fun readCurrentSellerUseCase(sellerRepository: SellerRepository) =
        ReadCurrentSellerUseCase(sellerRepository)

    @Bean
    open fun updateSellerUseCase(sellerRepository: SellerRepository) = UpdateSellerUseCase(sellerRepository)

    @Bean
    open fun readSocialNetworksUseCase(sellerRepository: SellerRepository) =
        ReadSocialNetworksUseCase(sellerRepository)

    @Bean
    open fun sellerUseCases(readCurrentSellerUseCase: ReadCurrentSellerUseCase,
                            updateSellerUseCase: UpdateSellerUseCase,
                            readSocialNetworksUseCase: ReadSocialNetworksUseCase) =
        SellerUseCases(readCurrentSellerUseCase, updateSellerUseCase, readSocialNetworksUseCase)
}
