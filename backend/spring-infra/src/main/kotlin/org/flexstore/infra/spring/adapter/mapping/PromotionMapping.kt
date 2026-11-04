package org.flexstore.infra.spring.adapter.mapping

import org.flexstore.domain.discount.Percentage
import org.flexstore.domain.product.NoPromotion
import org.flexstore.domain.product.PercentageOffPromotion
import org.flexstore.domain.product.Promotion
import org.flexstore.domain.product.PromotionEnd
import org.flexstore.domain.product.PromotionPeriod
import org.flexstore.domain.product.PromotionStart
import org.flexstore.infra.spring.adapter.rest.json.JsonPromotion
import java.math.BigDecimal
import java.time.Instant

// The promotion of a product as stored or sent: a percentage and the moments it starts and ends, all null when
// the product has no promotion
fun promotionOf(percentage: BigDecimal?, startsAt: Instant?, endsAt: Instant?): Promotion {
    if (percentage == null || startsAt == null || endsAt == null) return NoPromotion
    val period = PromotionPeriod(PromotionStart(startsAt), PromotionEnd(endsAt))
    return PercentageOffPromotion(Percentage(percentage), period)
}

fun Promotion.percentage(): BigDecimal? = when (this) {
    is PercentageOffPromotion -> percentage.value
    is NoPromotion -> null
}

fun Promotion.startsAt(): Instant? = when (this) {
    is PercentageOffPromotion -> period.start.value
    is NoPromotion -> null
}

fun Promotion.endsAt(): Instant? = when (this) {
    is PercentageOffPromotion -> period.end.value
    is NoPromotion -> null
}

// The percentage off the price right now: null when there is no promotion or it is not running
fun Promotion.runningPercentage(): BigDecimal? = if (isRunning()) percentage() else null

fun Promotion.toJson(): JsonPromotion? = when (this) {
    is PercentageOffPromotion -> JsonPromotion(percentage.value, startsAt().toString(), endsAt().toString())
    is NoPromotion -> null
}

fun JsonPromotion?.toDomain(): Promotion = when (this) {
    null -> NoPromotion
    else -> promotionOf(percentage, Instant.parse(startsAt), Instant.parse(endsAt))
}
