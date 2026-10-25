package org.flexstore.domain.sharedkernel

import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.Locale

class PriceTest : WithAssertions {

    @Test
    fun `a price formatted in French should have 2 decimals, a comma and its currency symbol`() {
        // Arrange
        val price = Price(Amount(BigDecimal("1598")), Currency.EUR)
        // Act
        val formattedPrice = price.formattedIn(Locale.FRANCE)
        // Assert
        assertThat(formattedPrice).isEqualTo("1\u202F598,00 €")
    }
}
