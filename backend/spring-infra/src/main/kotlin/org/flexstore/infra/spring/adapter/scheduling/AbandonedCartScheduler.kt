package org.flexstore.infra.spring.adapter.scheduling

import org.flexstore.domain.cart.AbandonedCartService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled

// Every 5 minutes, reminds the customers of the carts they left for the delay of the shop settings, when the
// reminder is turned on (see SchedulingConfig)
class AbandonedCartScheduler(private val abandonedCartService: AbandonedCartService) {

    private val logger = LoggerFactory.getLogger(AbandonedCartScheduler::class.java)

    @Scheduled(fixedDelayString = "PT5M", initialDelayString = "PT1M")
    fun remindAbandonedCarts() {
        val remindedCarts = abandonedCartService.remindAbandonedCarts().asList()
        remindedCarts.forEach { logger.info("Cart of {} reminded: left for a while", it.owner) }
    }
}
