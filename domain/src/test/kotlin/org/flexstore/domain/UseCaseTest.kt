package org.flexstore.domain

import kotlin.test.Test

class UseCaseTest {

    @Test
    fun `scenario`() {
        // given
        val actor = Actor(NonEmptyString("authenticated user"))
        val nominalFlow = NominalFlow(listOf(
            Step(NonEmptyString("step1")) { println("step1") },
            Step(NonEmptyString("step2")) { throw StepException(NonEmptyString("step2")) }
        ))
        val alternativeFlow = AlternativeFlow(listOf(
            Step(NonEmptyString("alternative1")) { println("alternative1") },
            Step(NonEmptyString("alternative2")) { println("alternative2") }
        ))
        // when / then
        try {
            actor.perform(nominalFlow)
        } catch (ne: NominalException) {
            actor.perform(alternativeFlow)
        }
    }
}