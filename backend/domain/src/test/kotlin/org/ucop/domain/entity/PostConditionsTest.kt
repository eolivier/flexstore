package org.ucop.domain.entity

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.catchThrowable
import org.junit.jupiter.api.Test

class PostConditionsTest {

    @Test
    fun `should check every postcondition, in order`() {
        // Arrange
        val checked = mutableListOf<String>()
        val postConditions = PostConditions(PostCondition<String> { checked.add("first $it") }, PostCondition { checked.add("second $it") })
        // Act
        postConditions.validate("request")
        // Assert
        assertThat(checked).containsExactly("first request", "second request")
    }

    @Test
    fun `should stop at the first postcondition that fails`() {
        // Arrange
        val checked = mutableListOf<String>()
        val postConditions = PostConditions(PostCondition<String> { throw IllegalStateException("failed") }, PostCondition { checked.add(it) })
        // Act
        val thrown = catchThrowable { postConditions.validate("request") }
        // Assert
        assertThat(thrown).hasMessage("failed")
        assertThat(checked).isEmpty()
    }

    @Test
    fun `no postcondition should accept any request`() {
        // Act
        val thrown = catchThrowable { PostConditions.none<String>().validate("request") }
        // Assert
        assertThat(thrown).isNull()
    }
}
