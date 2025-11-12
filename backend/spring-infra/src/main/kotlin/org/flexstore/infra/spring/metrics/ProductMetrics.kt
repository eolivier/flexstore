package org.flexstore.infra.spring.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import org.springframework.stereotype.Component

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

private const val FLEXSTORE_PRODUCTS_CREATED = "flexstore.products.created"

private const val FLEXSTORE_PRODUCTS_LIST_TIMER = "flexstore.products.list.timer"

private const val FLEXSTORE_INVENTORY_TOTAL = "flexstore.inventory.total"

@Component
class ProductMetrics(private val registry: MeterRegistry) {

    private val inventory = AtomicInteger(0)

    private val productCreatedCounter =  Counter.builder(FLEXSTORE_PRODUCTS_CREATED)
        .description("Number of products created")
        .register(registry)

    private val listTimer: Timer = Timer.builder(FLEXSTORE_PRODUCTS_LIST_TIMER)
        .description("Time to list products")
        .publishPercentiles(0.5, 0.95, 0.99)
        .publishPercentileHistogram()
        .register(registry) // -> flexstore_products_list_timer_seconds_*

    init {
        Gauge
            .builder(FLEXSTORE_INVENTORY_TOTAL, inventory) { it.get().toDouble() }
            .description("Total number of products")
            .baseUnit("items")
            .register(registry) // -> flexstore_inventory_total
    }


    fun incrementCreated() = productCreatedCounter.increment()
    fun recordList(durationNanos: Long) = listTimer.record(durationNanos, TimeUnit.NANOSECONDS)
    fun setInventory(total: Int) { inventory.set(total) }
}