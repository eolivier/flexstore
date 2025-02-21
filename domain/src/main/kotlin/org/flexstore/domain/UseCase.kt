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

interface Flow

data class NominalFlow(val steps: List<Step>, val nextUseCase: IUserCase = NoUseCase()):Flow
data class AlternativeFlow(val steps: List<Step>):Flow

data class Step(val name: NonEmptyString, val action: () -> Unit)

data class Actor(val name: NonEmptyString) {
    fun perform(nominalFlow: NominalFlow) {
        nominalFlow.steps.forEach { step ->
            println("[NOMINAL FLOW] Actor $name performs step ${step.name}")
            try {
                step.action()
            } catch (se: StepException) {
                throw NominalException(se.nonEmptyMessage)
            }
        }
    }
    fun perform(alternativeFlow: AlternativeFlow) {
        alternativeFlow.steps.forEach { step ->
            println("[ALTERNATIVE FLOW] Actor $name performs step ${step.name}")
            try {
                step.action()
            } catch (se: StepException) {
                throw AlternativeException(se.nonEmptyMessage)
            }
        }
    }
}

interface IUserCase

data class EffectiveUseCase(val actor: Actor, val nominalFlow: NominalFlow, val alternativeFlow: AlternativeFlow):IUserCase {
    fun execute() {
        try {
            actor.perform(nominalFlow)
            if(nominalFlow.nextUseCase is EffectiveUseCase) {
                nominalFlow.nextUseCase.execute()
            }
        } catch (ne: NominalException) {
            actor.perform(alternativeFlow)
        }
    }
}

class NoUseCase: IUserCase
