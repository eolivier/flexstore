package org.flexstore.domain.sharedkernel

import org.assertj.core.api.WithAssertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.streams.asStream

class QuantityTest : WithAssertions {

    companion object {
        @JvmStatic
        fun increaseTestCases() = sequenceOf(
            Arguments.of(Quantity(0), Quantity(1)),
            Arguments.of(Quantity(5), Quantity(6)),
            Arguments.of(Quantity(-1), Quantity(0))
        ).asStream()

        @JvmStatic
        fun decreaseTestCases() = sequenceOf(
            Arguments.of(Quantity(0), Quantity(-1)),
            Arguments.of(Quantity(5), Quantity(4)),
            Arguments.of(Quantity(-1), Quantity(-2))
        ).asStream()

        @JvmStatic
        fun changeQuantityTestCases() = sequenceOf(
            Arguments.of(Quantity(0), Quantity(0)),
            Arguments.of(Quantity(5), Quantity(8)),
            Arguments.of(Quantity(-1), Quantity(2))
        ).asStream()
    }

    @ParameterizedTest(name = "{0} increased is {1}")
    @MethodSource("increaseTestCases")
    fun `increasing a quantity should add one`(input: Quantity, expected: Quantity) {
        // Act
        val newQuantity = input.increase()
        // Assert
        assertThat(newQuantity).isEqualTo(expected)
        assertThat(newQuantity !== input).isTrue()
    }

    @ParameterizedTest(name = "{0} decreased is {1}")
    @MethodSource("decreaseTestCases")
    fun `decreasing a quantity should remove one`(input: Quantity, expected: Quantity) {
        // Act
        val newQuantity = input.decrease()
        // Assert
        assertThat(newQuantity).isEqualTo(expected)
        assertThat(newQuantity !== input).isTrue()
    }

    @ParameterizedTest(name = "{0} changed to {1}")
    @MethodSource("changeQuantityTestCases")
    fun `changing a quantity should give the new quantity`(input: Quantity, change: Quantity) {
        // Act
        val newQuantity = input.changeQuantity(change)
        // Assert
        assertThat(newQuantity).isEqualTo(change)
        assertThat(newQuantity !== input).isTrue()
    }
}
