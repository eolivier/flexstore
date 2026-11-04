package org.flexstore.domain.product

import org.flexstore.domain.discount.Percentage
import org.flexstore.domain.discount.PercentageOff
import org.flexstore.domain.sharedkernel.InstantProvider
import org.flexstore.domain.sharedkernel.Price
import java.time.Instant

// The moment, to the minute, from which a promotion lowers the price
data class PromotionStart(val value: Instant) {
    fun isReachedAt(instant: Instant) = instant >= value
}

// The moment, to the minute, from which a promotion stops lowering the price
data class PromotionEnd(val value: Instant) {
    fun isReachedAt(instant: Instant) = instant >= value

    fun isNotReachedAt(instant: Instant) = instant < value
}

// From its start (included) to its end (excluded): the end comes after the start
data class PromotionPeriod(val start: PromotionStart, val end: PromotionEnd) {

    fun isOngoingAt(instant: Instant) = start.isReachedAt(instant) && end.isNotReachedAt(instant)

    fun isInvalid() = end.isReachedAt(start.value)
}

// What the shop takes off the price of a product for a time: nothing, or a percentage during a period, the
// regular price being shown struck through next to the reduced one while the promotion runs
sealed interface Promotion {
    fun priceAfter(price: Price): Price
    fun isInvalid(): Boolean
    fun isRunning(): Boolean
}

data object NoPromotion : Promotion {
    override fun priceAfter(price: Price) = price
    override fun isInvalid() = false
    override fun isRunning() = false
}

data class PercentageOffPromotion(val percentage: Percentage, val period: PromotionPeriod) : Promotion {

    override fun priceAfter(price: Price) =
        if (isRunning()) price - PercentageOff(percentage).amountOff(price) else price

    override fun isInvalid() = percentage.isInvalid() || period.isInvalid()

    override fun isRunning() = period.isOngoingAt(InstantProvider.now())
}
