package org.flexstore.domain.order

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.order.model.FreeShippingEligibility
import org.flexstore.domain.order.model.ShippingLabel
import org.flexstore.domain.order.model.ShippingMethod
import org.flexstore.domain.order.model.ShippingMethodId
import org.flexstore.domain.order.model.ShippingMethodStatus
import org.flexstore.domain.settings.FreeShippingThreshold
import org.flexstore.domain.sharedkernel.*
import org.junit.jupiter.api.Test
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
        methodName: String,
        itemsAmount: BigDecimal,
        expectedCost: BigDecimal
    ) {
        // Arrange
        val method = if (methodName == "STANDARD") ShippingMethod.STANDARD else ShippingMethod.EXPRESS
        val itemsPrice = Price(Amount(itemsAmount), Currency.EUR)
        // Act
        val cost = method.costFor(itemsPrice, threshold)
        // Assert
        assertThat(cost.amount.value).isEqualByComparingTo(expectedCost)
    }

    @Test
    fun `a method should be refused without name or with a negative cost`() {
        // Arrange
        val cost = Price(Amount(BigDecimal.TEN), Currency.EUR)
        val unnamed = ShippingMethod(
            ShippingMethodId.nextId(), ShippingLabel(" "), cost, FreeShippingEligibility.ELIGIBLE
        )
        val cheaperThanFree = ShippingMethod(
            ShippingMethodId.nextId(), ShippingLabel("Relais"),
            Price(Amount(BigDecimal("-1")), Currency.EUR), FreeShippingEligibility.ELIGIBLE
        )
        // Assert
        assertThat(unnamed.isInvalid()).isTrue()
        assertThat(cheaperThanFree.isInvalid()).isTrue()
        assertThat(ShippingMethod.STANDARD.isInvalid()).isFalse()
    }

    @Test
    fun `a new method should be enabled`() {
        // Assert
        assertThat(ShippingMethod.STANDARD.status).isEqualTo(ShippingMethodStatus.ENABLED)
    }
}
