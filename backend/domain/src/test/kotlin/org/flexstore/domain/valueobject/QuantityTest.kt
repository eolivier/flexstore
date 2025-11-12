package org.flexstore.domain.valueobject

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

    @ParameterizedTest
    @MethodSource("increaseTestCases")
    fun `test increase`(input: Quantity, expected: Quantity) {
        // when
        val result = input.increase()
        // then
        assertThat(result).isEqualTo(expected)
        assertThat(result !== input).isTrue()
    }

    @ParameterizedTest
    @MethodSource("decreaseTestCases")
    fun `test decrease`(input: Quantity, expected: Quantity) {
        // when
        val result = input.decrease()
        // then
        assertThat(result).isEqualTo(expected)
        assertThat(result !== input).isTrue()
    }

    @ParameterizedTest
    @MethodSource("changeQuantityTestCases")
    fun `test changeQuantity`(input: Quantity, change: Quantity) {
        // when
        val result = input.changeQuantity(change)
        // then
        assertThat(result).isEqualTo(change)
        assertThat(result !== input).isTrue()
    }
}
