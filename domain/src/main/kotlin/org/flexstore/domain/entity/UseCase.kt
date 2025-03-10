package org.flexstore.domain.entity

import org.flexstore.domain.Name
import org.flexstore.domain.NominalException
import org.flexstore.domain.StepException

open class Actor(val name: Name)

data class PreCondition<T>(val validate: (T) -> Unit)
data class PreConditions(val preConditions: List<PreCondition<Any>>) {
    fun <T> validate(t: T) {
        preConditions.forEach { (it as PreCondition<T>).validate(t) }
    }
}
data class PostCondition<T>(val validate: (T) -> Unit)
data class Step<T>(val run: (T) -> Unit)

interface Scenario<T> {
    val steps: List<Step<T>>
    fun run(t: T) {
        steps.forEach { it.run(t) }
    }
}

data class NominalScenario<T>(override val steps: List<Step<T>>) : Scenario<T> {
    override fun run(t: T) {
        try {
            steps.forEach { it.run(t) }
        } catch (se: StepException) {
            throw NominalException(se.nonEmptyMessage)
        }
    }
}

data class AlternativeScenario<T>(override val steps: List<Step<T>>) : Scenario<T>

data class UseCase<T>(
    val preConditions: List<PreCondition<T>>,
    val nominalScenario: NominalScenario<T>,
    val postConditions: List<PostCondition<T>>
) {
    fun run(t: T) {
        preConditions.forEach { it.validate(t) }
        nominalScenario.run(t)
        postConditions.forEach { it.validate(t) }
    }
}

/*data class NominalScenario(val steps: List<Step<*>>, val nextUseCase: UserCase = NoUseCase()):Scenario {
    fun execute() {
        println("[NOMINAL SCENARIO : BEGIN]")
        steps.forEach { step ->

        }
        println("[NOMINAL SCENARIO : END]")
    }
}*/

/*data class Actor(val name: NonEmptyString) {
    fun perform(nominalScenario: NominalScenario) {
        nominalScenario.execute()
    }
    fun perform(alternativeScenario: AlternativeScenario) {
        alternativeScenario.steps.forEach { step ->
            println("[ALTERNATIVE SCENARIO : BEGIN] Actor $name performs step ${step.name}")
            try {
                step.execute()
            } catch (se: StepException) {
                throw AlternativeException(se.nonEmptyMessage)
            }
            println("[ALTERNATIVE SCENARIO : END] Actor $name has performed step ${step.name}")
        }
    }
}*/

interface UserCase

/*data class EffectiveUseCase(val actor: Actor, val nominalScenario: NominalScenario, val alternativeScenario: AlternativeScenario):UserCase {
    fun execute() {
        try {
            actor.perform(nominalScenario)
            if(nominalScenario.nextUseCase is EffectiveUseCase) {
                nominalScenario.nextUseCase.execute()
            }
        } catch (ne: NominalException) {
            actor.perform(alternativeScenario)
        }
    }
}*/

class NoUseCase: UserCase
