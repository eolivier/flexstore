package org.flexstore.domain.sales

import org.flexstore.domain.order.*
import org.flexstore.domain.sharedkernel.Price
import org.ucop.domain.NominalException
import org.flexstore.domain.sharedkernel.ErrorMessage
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

// The shop counts its days in its own time zone
private val SHOP_ZONE: ZoneId = ZoneId.of("Europe/Paris")

// A day of the shop, from midnight to midnight in its time zone
data class SalesDay(val value: LocalDate) {

    fun interval() = OrderInterval(
        value.atStartOfDay(SHOP_ZONE).toInstant(),
        value.plusDays(1).atStartOfDay(SHOP_ZONE).toInstant()
    )

    fun daysBefore(dayCount: DayCount) = SalesDay(value.minusDays(dayCount.value.toLong()))

    fun next() = SalesDay(value.plusDays(1))

    companion object {
        fun of(moment: Instant) = SalesDay(moment.atZone(SHOP_ZONE).toLocalDate())
    }
}

// A number of days: the sales are shown over the last 7, 30 or 90 days
data class DayCount(val value: Int) {

    fun isAllowed() = value in ALLOWED

    fun isNotAllowed() = !isAllowed()

    companion object {
        private val ALLOWED = listOf(7, 30, 90)
    }
}

// The last days of the shop, the last one included (a period of 7 days ending on the 31st starts on the 25th)
data class SalesPeriod(val lastDay: SalesDay, val dayCount: DayCount) {

    val firstDay get() = lastDay.daysBefore(DayCount(dayCount.value - 1))

    fun interval() = OrderInterval(firstDay.interval().start, lastDay.interval().end)

    fun days(): List<SalesDay> = generateSequence(firstDay) { day -> day.next() }
        .take(dayCount.value).toList()
}

// What the shop sold the days of a period: asked at a moment
data class SalesQuestion(val dayCount: DayCount, val askedAt: Instant) {

    fun period() = SalesPeriod(SalesDay.of(askedAt), dayCount)
}

// The orders of a day and what they brought, none and 0 for a day without order
data class DailySales(val day: SalesDay, val orderCount: OrderCount, val revenue: Price)

data class DailySalesSeries(private val dailySales: List<DailySales>) {

    // Read-only view for the adapters (JSON)
    fun asList(): List<DailySales> = dailySales.toList()
}

// The sales of a period: the orders paid (not cancelled nor refunded) placed during it, what they brought, one
// day after the other, and the products sold the most
data class SalesDashboard(
    val period: SalesPeriod,
    val revenue: Price,
    val orderCount: OrderCount,
    val dailySales: DailySalesSeries,
    val bestSellers: BestSellers
) {
    companion object {
        private val BEST_SELLER_COUNT = BestSellerCount(5)

        fun of(orders: Orders, period: SalesPeriod): SalesDashboard {
            val sold = orders.paid().placedDuring(period.interval())
            val dailySales = period.days().map { day ->
                val ofTheDay = sold.placedDuring(day.interval())
                DailySales(day, ofTheDay.count(), ofTheDay.revenue())
            }
            return SalesDashboard(
                period, sold.revenue(), sold.count(), DailySalesSeries(dailySales),
                sold.lines().bestSellers(BEST_SELLER_COUNT)
            )
        }
    }
}

data class InvalidSalesPeriodException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
