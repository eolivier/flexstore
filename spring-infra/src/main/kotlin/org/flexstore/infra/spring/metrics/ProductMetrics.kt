package org.flexstore.infra.spring.metrics

import io.micrometer.core.instrument.Counter
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Timer
import jakarta.annotation.PostConstruct
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

private const val FLEXSTORE_PRODUCTS_CREATED = "flexstore.products.created"

private const val FLEXSTORE_PRODUCTS_LIST_TIMER = "flexstore.products.list.timer"

private const val FLEXSTORE_INVENTORY_TOTAL = "flexstore.inventory.total"

@Component
class ProductMetrics(private val registry: MeterRegistry) {

    private val log = LoggerFactory.getLogger(javaClass)
    //private val productCreatedCounter = registry.counter(FLEXSTORE_PRODUCTS_CREATED)
    private val productCreatedCounter =  Counter.builder(FLEXSTORE_PRODUCTS_CREATED)
        .description("Number of products created")
        .register(registry)
    private val inventoryGaugeValue = AtomicInteger(0)
    private val listTimer: Timer = Timer.builder(FLEXSTORE_PRODUCTS_LIST_TIMER)
        .description("Time to list products")
        .publishPercentiles(0.5, 0.95, 0.99)
        .publishPercentileHistogram()
        .register(registry)

    init {
        registry.gauge(FLEXSTORE_INVENTORY_TOTAL, listOf(Tag.of("unit", "items")), inventoryGaugeValue)
    }

    @PostConstruct
    fun logRegistry() {
        log.info("MeterRegistry in ProductMetrics = {}", registry::class.qualifiedName)
    }

    fun incrementCreated() = productCreatedCounter.increment()
    fun recordList(durationNanos: Long) = listTimer.record(durationNanos, TimeUnit.NANOSECONDS)
    fun setInventory(total: Int) { inventoryGaugeValue.set(total) }
}