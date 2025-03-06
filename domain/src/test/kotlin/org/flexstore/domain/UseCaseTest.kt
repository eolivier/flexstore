package org.flexstore.domain

import kotlin.test.Test

class UseCaseTest {

    @Test
    fun `scenario`() {
        // given
        val actor = Actor(NonEmptyString("authenticated user"))
        val nominalScenario = NominalScenario(listOf(
            Step(NonEmptyString("step1")),
            Step(NonEmptyString("step2"))
        ))
        val alternativeScenario = AlternativeScenario(listOf(
            Step(NonEmptyString("alternative1")),
            Step(NonEmptyString("alternative2"))
        ))
        // when / then
        try {
            actor.perform(nominalScenario)
        } catch (ne: NominalException) {
            actor.perform(alternativeScenario)
        }
    }
}