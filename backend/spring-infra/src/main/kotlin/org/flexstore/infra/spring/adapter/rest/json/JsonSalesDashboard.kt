package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.order.BestSeller
import org.flexstore.domain.sales.DailySales
import org.flexstore.domain.sales.SalesDashboard
import java.math.BigDecimal

// The sales of the shop over a period: the revenue (VAT and shipping included, discounts taken off) and the
// number of the orders paid, day by day, and the products sold the most
data class JsonSalesDashboard(
    @JsonProperty("firstDay") val firstDay: String,
    @JsonProperty("lastDay") val lastDay: String,
    @JsonProperty("currency") val currency: String,
    @JsonProperty("revenue") val revenue: BigDecimal,
    @JsonProperty("orderCount") val orderCount: Int,
    @JsonProperty("days") val days: List<JsonDailySales>,
    @JsonProperty("bestSellers") val bestSellers: List<JsonBestSeller>
) {
    constructor(dashboard: SalesDashboard) : this(
        dashboard.period.firstDay.value.toString(),
        dashboard.period.lastDay.value.toString(),
        dashboard.revenue.currency.symbol,
        dashboard.revenue.amount.value,
        dashboard.orderCount.value,
        dashboard.dailySales.asList().map { JsonDailySales(it) },
        dashboard.bestSellers.asList().map { JsonBestSeller(it) }
    )
}

// The orders of a day (a day of the shop, in its time zone) and what they brought
data class JsonDailySales(
    @JsonProperty("day") val day: String,
    @JsonProperty("orderCount") val orderCount: Int,
    @JsonProperty("revenue") val revenue: BigDecimal
) {
    constructor(dailySales: DailySales) : this(
        dailySales.day.value.toString(), dailySales.orderCount.value, dailySales.revenue.amount.value
    )
}

// A product sold: its id, its name when it was ordered, the units sold and what the lines brought
data class JsonBestSeller(
    @JsonProperty("productId") val productId: String,
    @JsonProperty("productName") val productName: String,
    @JsonProperty("unitsSold") val unitsSold: Int,
    @JsonProperty("revenue") val revenue: BigDecimal
) {
    constructor(bestSeller: BestSeller) : this(
        bestSeller.product.productId.id.value,
        bestSeller.product.productName.value,
        bestSeller.unitsSold.value,
        bestSeller.revenue.amount.value
    )
}
