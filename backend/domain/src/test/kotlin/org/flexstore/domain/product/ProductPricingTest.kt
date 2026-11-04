package org.flexstore.domain.product

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.discount.Percentage
import org.flexstore.domain.sharedkernel.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant

class ProductPricingTest : WithAssertions {

    private val now = Instant.parse("2026-11-10T12:00:00Z")
    private val regularPrice = Price(Amount(BigDecimal(80)), Currency.EUR)
    private val hour = Duration.ofHours(1)

    @BeforeEach
    fun fixTheMoment() {
        InstantProvider.fixAt(now)
    }

    @AfterEach
    fun resetTheMoment() {
        InstantProvider.reset()
    }

    private fun promotion(percentage: Int, starts: Instant, ends: Instant): Promotion {
        val period = PromotionPeriod(PromotionStart(starts), PromotionEnd(ends))
        return PercentageOffPromotion(Percentage(BigDecimal(percentage)), period)
    }

    private fun runningPromotion(percentage: Int) = promotion(percentage, now - hour, now + hour)

    @Test
    fun `without promotion, the current price should be the regular price`() {
        // Arrange
        val pricing = ProductPricing(regularPrice)
        // Act
        val currentPrice = pricing.currentPrice
        // Assert
        assertThat(currentPrice).isEqualTo(regularPrice)
    }

    @ParameterizedTest(name = "a running promotion of {0} % takes the price to {1}")
    @CsvSource("25, 60.00", "50, 40.00", "100, 0.00")
    fun `with a running promotion, the current price should be the regular price less the percentage`(
        percentage: Int,
        expectedAmount: String
    ) {
        // Arrange
        val pricing = ProductPricing(regularPrice, VatRate.STANDARD, runningPromotion(percentage))
        // Act
        val currentPrice = pricing.currentPrice
        // Assert
        assertThat(currentPrice.amount.value).isEqualByComparingTo(expectedAmount)
    }

    @Test
    fun `a promotion that starts later should not lower the price yet`() {
        // Arrange
        val comingPromotion = promotion(25, now + hour, now + hour + hour)
        val pricing = ProductPricing(regularPrice, VatRate.STANDARD, comingPromotion)
        // Act
        val currentPrice = pricing.currentPrice
        // Assert
        assertThat(currentPrice).isEqualTo(regularPrice)
        assertThat(comingPromotion.isRunning()).isFalse()
    }

    @Test
    fun `a promotion that is over should not lower the price any more`() {
        // Arrange
        val pastPromotion = promotion(25, now - hour - hour, now - hour)
        val pricing = ProductPricing(regularPrice, VatRate.STANDARD, pastPromotion)
        // Act
        val currentPrice = pricing.currentPrice
        // Assert
        assertThat(currentPrice).isEqualTo(regularPrice)
        assertThat(pastPromotion.isRunning()).isFalse()
    }

    @Test
    fun `a promotion should lower the price from its start included`() {
        // Arrange
        val startingNow = promotion(25, now, now + hour)
        // Act
        val running = startingNow.isRunning()
        // Assert
        assertThat(running).isTrue()
    }

    @Test
    fun `a promotion should stop lowering the price at its end`() {
        // Arrange
        val endingNow = promotion(25, now - hour, now)
        // Act
        val running = endingNow.isRunning()
        // Assert
        assertThat(running).isFalse()
    }

    @Test
    fun `a promotion should leave the regular price unchanged`() {
        // Arrange
        val pricing = ProductPricing(regularPrice, VatRate.STANDARD, runningPromotion(25))
        // Act
        val price = pricing.price
        // Assert
        assertThat(price).isEqualTo(regularPrice)
    }

    @ParameterizedTest(name = "a promotion from {0} to {1} minutes is invalid")
    @CsvSource("0, 0", "5, 0", "5, 5")
    fun `a promotion that does not end after it starts should be invalid`(startMinutes: Long, endMinutes: Long) {
        // Arrange
        val starts = now.plus(Duration.ofMinutes(startMinutes))
        val ends = now.plus(Duration.ofMinutes(endMinutes))
        // Act
        val invalid = promotion(25, starts, ends).isInvalid()
        // Assert
        assertThat(invalid).isTrue()
    }

    @ParameterizedTest(name = "a promotion of {0} % is invalid")
    @CsvSource("0", "-10", "101")
    fun `a promotion with a percentage out of range should be invalid`(percentage: Int) {
        // Act
        val invalid = runningPromotion(percentage).isInvalid()
        // Assert
        assertThat(invalid).isTrue()
    }
}
