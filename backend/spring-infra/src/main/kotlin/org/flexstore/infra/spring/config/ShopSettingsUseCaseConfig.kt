package org.flexstore.infra.spring.config

import org.flexstore.domain.settings.*
import org.flexstore.domain.settings.usecase.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The use cases of the shop settings sub-domain, declared step by step: use cases, then their group
@Configuration
open class ShopSettingsUseCaseConfig {

    @Bean
    open fun readShopSettingsUseCase(shopSettingsRepository: ShopSettingsRepository): ReadShopSettingsUseCase {
        return ReadShopSettingsUseCase(shopSettingsRepository)
    }

    @Bean
    open fun readAdministeredShopSettingsUseCase(shopSettingsRepository: ShopSettingsRepository) =
        ReadAdministeredShopSettingsUseCase(shopSettingsRepository)

    @Bean
    open fun updateShopSettingsUseCase(shopSettingsRepository: ShopSettingsRepository) =
        UpdateShopSettingsUseCase(shopSettingsRepository)
}
