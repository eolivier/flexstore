package org.ucop.domain.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class NominalScenarioTest {

    @Test
    fun `should run the steps given without a list, in order`() {
        // Arrange
        val runSteps = mutableListOf<String>()
        val scenario = NominalScenario(Step<String> { runSteps.add("first $it") }, Step { runSteps.add("second $it") })
        // Act
        scenario.unfold("call")
        // Assert
        assertThat(runSteps).containsExactly("first call", "second call")
    }
}
