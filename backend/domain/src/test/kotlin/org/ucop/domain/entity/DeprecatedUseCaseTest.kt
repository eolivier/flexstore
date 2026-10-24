package org.ucop.domain.entity

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DeprecatedUseCaseTest {

    @Test
    fun `should run the preconditions, the nominal scenario and the postconditions`() {
        // Arrange
        val basket = mutableListOf<String>()
        val basketIsEmptyCondition = PreCondition<MutableList<String>> { assert(it.isEmpty()) }
        val addOneItemStep = Step<MutableList<String>>(run = { it.add("item1") })
        val basketHasOneItemCondition = PostCondition<MutableList<String>> { assert(it.size == 1) }
        val addOneItemToBasketDeprecatedUseCase = DeprecatedUseCase(
            preConditions = listOf(basketIsEmptyCondition),
            nominalScenario = NominalScenario(listOf(addOneItemStep)),
            postConditions = listOf(basketHasOneItemCondition)
        )
        // Act
        addOneItemToBasketDeprecatedUseCase.run(basket)
        // Assert
        assertThat(basket).containsExactly("item1")
    }
}
