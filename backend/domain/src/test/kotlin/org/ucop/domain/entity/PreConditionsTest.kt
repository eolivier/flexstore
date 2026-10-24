package org.ucop.domain.entity

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test

class PreConditionsTest {

    @Test
    fun `should check every precondition, in order`() {
        // Arrange
        val checked = mutableListOf<String>()
        val preConditions = PreConditions(PreCondition<String> { checked.add("first $it") }, PreCondition { checked.add("second $it") })
        // Act
        preConditions.validate("request")
        // Assert
        assertThat(checked).containsExactly("first request", "second request")
    }

    @Test
    fun `should stop at the first precondition that fails`() {
        // Arrange
        val checked = mutableListOf<String>()
        val preConditions = PreConditions(PreCondition<String> { throw IllegalStateException("failed") }, PreCondition { checked.add(it) })
        // Act
        val thrown = catchThrowable { preConditions.validate("request") }
        // Assert
        assertThat(thrown).hasMessage("failed")
        assertThat(checked).isEmpty()
    }

    @Test
    fun `no precondition should accept any request`() {
        // Act
        val thrown = catchThrowable { PreConditions.none<String>().validate("request") }
        // Assert
        assertThat(thrown).isNull()
    }
}
