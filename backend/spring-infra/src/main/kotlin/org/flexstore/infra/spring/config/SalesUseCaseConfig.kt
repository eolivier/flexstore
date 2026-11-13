package org.flexstore.infra.spring.config

import org.flexstore.domain.order.*
import org.flexstore.domain.sales.usecase.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// The use cases of the sales sub-domain, declared step by step: use cases, then their group
@Configuration
open class SalesUseCaseConfig {

    @Bean
    open fun readSalesDashboardUseCase(orderRepository: OrderRepository) =
        ReadSalesDashboardUseCase(orderRepository)
}
