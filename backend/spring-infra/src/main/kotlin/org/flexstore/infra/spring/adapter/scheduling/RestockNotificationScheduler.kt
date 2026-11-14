package org.flexstore.infra.spring.adapter.scheduling

import org.flexstore.domain.restock.RestockNotificationService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled

// Every minute, notifies the customers who wait for a product that is back in stock (see SchedulingConfig)
class RestockNotificationScheduler(private val restockNotificationService: RestockNotificationService) {

    private val logger = LoggerFactory.getLogger(RestockNotificationScheduler::class.java)

    @Scheduled(fixedDelayString = "PT1M", initialDelayString = "PT1M")
    fun notifyRestockedProducts() {
        val notifiedSubscriptions = restockNotificationService.notifyRestockedProducts().asList()
        notifiedSubscriptions.forEach {
            logger.info("Customer {} notified: product {} is back in stock", it.customer.value, it.productId.id.value)
        }
    }
}
