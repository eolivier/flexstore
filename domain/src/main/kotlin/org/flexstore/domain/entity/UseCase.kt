package org.flexstore.domain.entity

import org.flexstore.domain.AlternativeException
import org.flexstore.domain.Name
import org.flexstore.domain.NominalException
import org.flexstore.domain.StepException
import kotlin.reflect.KClass

open class Actor(val name: Name)

open class PreCondition<T>(val validate: (T) -> Unit)
class EmptyPreCondition<T> : PreCondition<T>({ /* No validation */ })

data class PreConditions(val preConditions: List<PreCondition<Any>>) {
    fun <T> validate(t: T) {
        preConditions.forEach { (it as PreCondition<T>).validate(t) }
    }
}

open class PostCondition<T>(val validate: (T) -> Unit)
class EmptyPostCondition<T> : PostCondition<T>({ /* No validation */ })

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

data class AlternativeScenario<T>(override val steps: List<Step<T>>) : Scenario<T> {
    override fun run(t: T) {
        try {
            steps.forEach { it.run(t) }
        } catch (se: StepException) {
            throw AlternativeException(se.nonEmptyMessage)
        }
    }
}

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

interface NewUseCase<T> {

    fun getPreConditions():List<PreCondition<T>>
    fun getNominalScenario():NominalScenario<T>
    fun getPostConditions():List<PostCondition<T>>

    fun getAlternativeScenarii():Map<KClass<out NominalException>, AlternativeScenario<T>> = emptyMap()

    fun run(t: T) {
        try {
            getPreConditions().forEach { it.validate(t) }
            getNominalScenario().run(t)
            getPostConditions().forEach { it.validate(t) }
        } catch (ne: NominalException) {
            getAlternativeScenarii().forEach { (_, value) -> value.run(t) }
        }
    }
}

class EmptyUseCase<T> : NewUseCase<T> {

    override fun getPreConditions(): List<PreCondition<T>> = listOf(EmptyPreCondition())

    override fun getNominalScenario(): NominalScenario<T> = NominalScenario(listOf())

    override fun getPostConditions(): List<PostCondition<T>> = listOf(EmptyPostCondition())

    override fun getAlternativeScenarii(): Map<KClass<out NominalException>, AlternativeScenario<T>> = emptyMap()

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
