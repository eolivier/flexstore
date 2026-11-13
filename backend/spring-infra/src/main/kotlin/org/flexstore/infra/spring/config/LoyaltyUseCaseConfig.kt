package org.flexstore.infra.spring.config

import org.flexstore.domain.loyalty.usecase.*
import org.flexstore.domain.order.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The use cases of the loyalty sub-domain, declared step by step: use cases, then their group
@Configuration
open class LoyaltyUseCaseConfig {

    @Bean
    open fun readLoyaltyPointsUseCase(orderRepository: OrderRepository) =
        ReadLoyaltyPointsUseCase(orderRepository)

    @Bean
    open fun loyaltyUseCases(readLoyaltyPointsUseCase: ReadLoyaltyPointsUseCase) =
        LoyaltyUseCases(readLoyaltyPointsUseCase)
}
