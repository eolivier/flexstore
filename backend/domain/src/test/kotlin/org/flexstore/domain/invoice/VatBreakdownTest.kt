package org.flexstore.domain.invoice

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.sharedkernel.*
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal

class VatBreakdownTest : WithAssertions {

    @ParameterizedTest(name = "{0} € with VAT is {1} € without VAT and {2} € of VAT")
    @CsvSource("120, 100.00, 20.00", "34.90, 29.08, 5.82", "0.01, 0.01, 0.00", "0, 0.00, 0.00")
    fun `a price with VAT should split into its amount without VAT and its VAT, rounded to the cent`(
        withVat: BigDecimal,
        withoutVat: BigDecimal,
        vat: BigDecimal
    ) {
        // Arrange
        val vatBreakdown = VatBreakdown(Price(Amount(withVat), Currency.EUR), VatRate.STANDARD)
        // Act
        val priceExcludingVat = vatBreakdown.priceExcludingVat()
        // Assert
        assertThat(priceExcludingVat.amount.value).isEqualByComparingTo(withoutVat)
        assertThat(vatBreakdown.vat().amount.value).isEqualByComparingTo(vat)
    }
}
