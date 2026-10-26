package org.flexstore.infra.spring.adapter.scheduling

import org.flexstore.domain.order.usecase.UnpaidOrderService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled

// Every minute, cancels the orders not paid within the payment delay of the shop settings (see SchedulingConfig)
class UnpaidOrderScheduler(private val unpaidOrderService: UnpaidOrderService) {

    private val logger = LoggerFactory.getLogger(UnpaidOrderScheduler::class.java)

    @Scheduled(fixedDelayString = "PT1M", initialDelayString = "PT1M")
    fun cancelLateOrders() {
        val cancelledOrders = unpaidOrderService.cancelLateOrders().asList()
        cancelledOrders.forEach { logger.info("Order {} cancelled: not paid in time", it.number()) }
    }
}
