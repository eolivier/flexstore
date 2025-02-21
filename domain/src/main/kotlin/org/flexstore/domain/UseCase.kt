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

data class NominalFlow(val steps: List<Step>)
data class AlternativeFlow(val steps: List<Step>)

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

