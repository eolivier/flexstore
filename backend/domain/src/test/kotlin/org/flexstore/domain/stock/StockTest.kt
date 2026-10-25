package org.flexstore.domain.stock

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.product.ProductId
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.sharedkernel.Quantity
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

class StockTest : WithAssertions {

    private val deskLampId = ProductId(Identity("desk-lamp"))

    @ParameterizedTest(name = "{0} units in stock cover {1} ordered: {2}")
    @CsvSource("3, 2, true", "3, 3, true", "3, 4, false", "0, 1, false")
    fun `a stock should cover a quantity up to its units`(units: Int, ordered: Int, covered: Boolean) {
        // Arrange
        val stock = Stock(deskLampId, StockQuantity(units))
        // Act
        val covers = stock.covers(Quantity(ordered))
        // Assert
        assertThat(covers).isEqualTo(covered)
    }

    @ParameterizedTest(name = "{0} units in stock, {1} paid: {2} left")
    @CsvSource("5, 2, 3", "2, 2, 0", "1, 2, 0")
    fun `a paid quantity should be taken out of the stock, which never goes below zero`(
        units: Int,
        paid: Int,
        left: Int
    ) {
        // Arrange
        val stock = Stock(deskLampId, StockQuantity(units))
        // Act
        val withdrawnStock = stock.withdrawn(Quantity(paid))
        // Assert
        assertThat(withdrawnStock.quantity).isEqualTo(StockQuantity(left))
    }
}
