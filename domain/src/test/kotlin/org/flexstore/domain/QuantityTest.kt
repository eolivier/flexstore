package org.flexstore.domain

import org.assertj.core.api.WithAssertions
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class QuantityTest : WithAssertions {

    companion object {
        @JvmStatic
        fun increaseTestCases(): Stream<Arguments> = Stream.of(
            Arguments.of(Quantity(0), Quantity(1)),
            Arguments.of(Quantity(5), Quantity(6)),
            Arguments.of(Quantity(-1), Quantity(0))
        )

        @JvmStatic
        fun decreaseTestCases(): Stream<Arguments> = Stream.of(
            Arguments.of(Quantity(0), Quantity(-1)),
            Arguments.of(Quantity(5), Quantity(4)),
            Arguments.of(Quantity(-1), Quantity(-2))
        )

        @JvmStatic
        fun changeQuantityTestCases(): Stream<Arguments> = Stream.of(
            Arguments.of(Quantity(0), Quantity(0), Quantity(0)),
            Arguments.of(Quantity(5), Quantity(3), Quantity(8)),
            Arguments.of(Quantity(-1), Quantity(2), Quantity(1))
        )
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
    fun `test changeQuantity`(input: Quantity, change: Quantity, expected: Quantity) {
        // when
        val result = input.changeQuantity(change)
        // then
        assertThat(result).isEqualTo(expected)
        assertThat(result !== input).isTrue()
    }
}
