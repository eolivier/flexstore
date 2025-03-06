package org.flexstore.domain

import org.junit.jupiter.api.Test

class NewUseCaseTest {

    @Test
    fun `should return hello from domain`() {
        // given
        val preCondition1 = Precondition(NonEmptyString("the first condition must be true"))
        val preCondition2 = Precondition(NonEmptyString("the second condition must be false"))
        val preConditions = listOf(preCondition1, preCondition2)
        val nominalScenario = NominalScenario(listOf(
            Step(NonEmptyString("step1")),
            Step(NonEmptyString("step2")),
        ))
        val postCondition1 = Postcondition(NonEmptyString("the variable x must be updated"))
        val postCondition2 = Postcondition(NonEmptyString("the variable y must be updated"))
        val postConditions = listOf(postCondition1, postCondition2)

        val newUseCase  = NewUseCase(preConditions, nominalScenario, postConditions)
        // when
        newUseCase.execute();
        // then
        //assertThat(message).isEqualTo("Hello from Domain!")
    }
}