package org.flexstore.domain.discount

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.sharedkernel.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import java.time.Instant

class DiscountCodeTest : WithAssertions {

    private fun euros(amount: String) = Price(Amount(BigDecimal(amount)), Currency.EUR)

    @ParameterizedTest(name = "{0} % off {1} € takes {2} € off")
    @CsvSource("10, 70, 7.00", "15, 33.33, 5.00", "100, 25, 25.00")
    fun `a percentage should take its share of the items, rounded to the cent`(
        percent: BigDecimal,
        items: String,
        expected: String
    ) {
        // Arrange
        val reduction = PercentageOff(Percentage(percent))
        // Act
        val amountOff = reduction.amountOff(euros(items))
        // Assert
        assertThat(amountOff.amount.value).isEqualByComparingTo(expected)
    }

    @ParameterizedTest(name = "5 € off {0} € takes {1} € off")
    @CsvSource("70, 5", "3, 3")
    fun `an amount should be taken off, never more than the price of the items`(
        items: String,
        expected: String
    ) {
        // Arrange
        val reduction = AmountOff(euros("5"))
        // Act
        val amountOff = reduction.amountOff(euros(items))
        // Assert
        assertThat(amountOff.amount.value).isEqualByComparingTo(expected)
    }

    @Test
    fun `a discount code should be used up once used by as many orders as allowed`() {
        // Arrange
        val validity = DiscountValidity(ExpiryDate(Instant.parse("2026-12-31T23:00:00Z")), UsageLimit(2))
        val discountCode = DiscountCode(PromoCode("TWICE"), DiscountOffer(AmountOff(euros("5")), validity))
        // Act
        val usedTwice = discountCode.usedOnceMore().usedOnceMore()
        // Assert
        assertThat(discountCode.usedOnceMore().isUsedUp()).isFalse()
        assertThat(usedTwice.isUsedUp()).isTrue()
    }

    @Test
    fun `a typed code should be trimmed and put in upper case`() {
        // Act
        val code = PromoCode.from("  welcome10 ")
        // Assert
        assertThat(code).isEqualTo(PromoCode("WELCOME10"))
    }
}
