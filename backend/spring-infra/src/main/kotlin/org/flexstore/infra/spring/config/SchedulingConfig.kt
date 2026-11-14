package org.flexstore.infra.spring.config

import org.flexstore.domain.cart.AbandonedCartService
import org.flexstore.domain.order.service.UnpaidOrderService
import org.flexstore.domain.restock.RestockNotificationService
import org.flexstore.infra.spring.adapter.scheduling.AbandonedCartScheduler
import org.flexstore.infra.spring.adapter.scheduling.RestockNotificationScheduler
import org.flexstore.infra.spring.adapter.scheduling.UnpaidOrderScheduler
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

// The tasks run regularly; turned off in the integration tests (flexstore.scheduling.enabled), which call the
// services themselves
@Configuration
@EnableScheduling
@ConditionalOnProperty("flexstore.scheduling.enabled", havingValue = "true", matchIfMissing = true)
open class SchedulingConfig {

    @Bean
    open fun unpaidOrderScheduler(unpaidOrderService: UnpaidOrderService) =
        UnpaidOrderScheduler(unpaidOrderService)

    @Bean
    open fun abandonedCartScheduler(abandonedCartService: AbandonedCartService) =
        AbandonedCartScheduler(abandonedCartService)

    @Bean
    open fun restockNotificationScheduler(restockNotificationService: RestockNotificationService) =
        RestockNotificationScheduler(restockNotificationService)
}
