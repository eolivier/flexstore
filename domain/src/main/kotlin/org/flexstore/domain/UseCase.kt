package org.flexstore.domain

interface UseCase<T> {

    fun preConditions()
    fun nominal()
    fun alternative()
    fun postConditions()

    fun scenario(t: T) {
        preConditions()
        try {
            nominal()
        } catch (ne: NominalException) {
            alternative()
        }
        postConditions()
    }
}

data class Precondition(val name: NonEmptyString){
    fun execute() {
        println("[PRECONDITION] $name")
    }
}
data class Postcondition(val name: NonEmptyString){
    fun execute() {
        println("[POSTCONDITION] $name")
    }
}

data class NewUseCase(
    val precConditions: List<Precondition>,
    val nominalScenario: NominalScenario,
    val postConditions: List<Postcondition>
) {
    fun execute() {
        precConditions.forEach { it.execute() }
        nominalScenario.execute()
        postConditions.forEach { it.execute() }
    }
}

interface Scenario

data class NominalScenario(val steps: List<Step>, val nextUseCase: UserCase = NoUseCase()):Scenario {
    fun execute() {
        println("[NOMINAL SCENARIO : BEGIN]")
        steps.forEach { step ->
            try {
                step.execute()
            } catch (se: StepException) {
                throw NominalException(se.nonEmptyMessage)
            }
        }
        println("[NOMINAL SCENARIO : END]")
    }
}
data class AlternativeScenario(val steps: List<Step>): Scenario

data class Step(val name: NonEmptyString) {
    fun execute() {
        println("[STEP] Step $name")
    }
}

data class Actor(val name: NonEmptyString) {
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
}

interface UserCase

data class EffectiveUseCase(val actor: Actor, val nominalScenario: NominalScenario, val alternativeScenario: AlternativeScenario):UserCase {
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
}

class NoUseCase: UserCase
