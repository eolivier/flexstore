package org.flexstore.infra.spring.config

import org.flexstore.domain.pagecontent.ShopPageContentRepository
import org.flexstore.domain.pagecontent.usecase.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The use cases of the shop page content sub-domain, declared step by step: use cases, then their group
@Configuration
open class ShopPageContentUseCaseConfig {

    @Bean
    open fun readShopPageContentUseCase(shopPageContentRepository: ShopPageContentRepository) =
        ReadShopPageContentUseCase(shopPageContentRepository)

    @Bean
    open fun editShopPageContentUseCase(shopPageContentRepository: ShopPageContentRepository) =
        EditShopPageContentUseCase(shopPageContentRepository)

    @Bean
    open fun shopPageContentUseCases(
        readShopPageContentUseCase: ReadShopPageContentUseCase,
        editShopPageContentUseCase: EditShopPageContentUseCase
    ) = ShopPageContentUseCases(readShopPageContentUseCase, editShopPageContentUseCase)
}
