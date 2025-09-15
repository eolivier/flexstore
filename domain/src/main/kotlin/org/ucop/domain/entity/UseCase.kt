package org.ucop.domain.entity

import org.flexstore.domain.valueobject.Name
import org.ucop.domain.AlternativeException
import org.ucop.domain.NominalException
import org.ucop.domain.StepException
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
    fun unfold(t: T) {
        steps.forEach { it.run(t) }
    }
}

data class NominalScenario<T>(override val steps: List<Step<T>>) : Scenario<T> {
    override fun unfold(t: T) {
        try {
            steps.forEach { it.run(t) }
        } catch (se: StepException) {
            throw NominalException(se.nonEmptyMessage)
        }
    }
}

data class AlternativeScenario<T>(override val steps: List<Step<T>>) : Scenario<T> {
    override fun unfold(t: T) {
        try {
            steps.forEach { it.run(t) }
        } catch (se: StepException) {
            throw AlternativeException(se.nonEmptyMessage)
        }
    }
}

data class DeprecatedUseCase<T>(
    val preConditions: List<PreCondition<T>>,
    val nominalScenario: NominalScenario<T>,
    val postConditions: List<PostCondition<T>>
) {
    fun run(t: T) {
        preConditions.forEach { it.validate(t) }
        nominalScenario.unfold(t)
        postConditions.forEach { it.validate(t) }
    }
}

interface UseCase<T> {

    fun getPreConditions():List<PreCondition<T>>
    fun getNominalScenario(): NominalScenario<T>
    fun getPostConditions():List<PostCondition<T>>

    fun getAlternativeScenarii():Map<KClass<out NominalException>, AlternativeScenario<T>> = emptyMap()

    fun unfold(t: T) {
        try {
            getPreConditions().forEach { it.validate(t) }
            getNominalScenario().unfold(t)
            getPostConditions().forEach { it.validate(t) }
        } catch (ne: NominalException) {
            if (getAlternativeScenarii().containsKey(ne::class)) {
                getAlternativeScenarii()[ne::class]?.unfold(t)
            } else {
                throw ne
            }
        }
    }
}

class EmptyUseCase<T> : UseCase<T> {

    override fun getPreConditions(): List<PreCondition<T>> = listOf(EmptyPreCondition())

    override fun getNominalScenario(): NominalScenario<T> = NominalScenario(listOf())

    override fun getPostConditions(): List<PostCondition<T>> = listOf(EmptyPostCondition())

    override fun getAlternativeScenarii(): Map<KClass<out NominalException>, AlternativeScenario<T>> = emptyMap()

}
