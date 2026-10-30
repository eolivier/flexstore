package org.flexstore.domain.order

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.settings.FreeShippingThreshold
import org.flexstore.domain.sharedkernel.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal

class ShippingMethodTest : WithAssertions {

    private val threshold = FreeShippingThreshold(Price(Amount(BigDecimal(50)), Currency.EUR))

    @ParameterizedTest(name = "{0} shipping of {1} € of items costs {2} €")
    @CsvSource(
        "STANDARD, 49.99, 4.90",
        "STANDARD, 50, 0",
        "STANDARD, 120, 0",
        "EXPRESS, 49.99, 9.90",
        "EXPRESS, 120, 9.90",
    )
    fun `the standard shipping should be free from the threshold, the express one never`(
        method: ShippingMethod,
        itemsAmount: BigDecimal,
        expectedCost: BigDecimal
    ) {
        // Arrange
        val itemsPrice = Price(Amount(itemsAmount), Currency.EUR)
        // Act
        val cost = method.costFor(itemsPrice, threshold)
        // Assert
        assertThat(cost.amount.value).isEqualByComparingTo(expectedCost)
    }
}
