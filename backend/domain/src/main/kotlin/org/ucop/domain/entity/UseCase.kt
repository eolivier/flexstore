package org.ucop.domain.entity

import org.flexstore.domain.sharedkernel.*
import org.ucop.domain.AlternativeException
import org.ucop.domain.NominalException
import org.ucop.domain.StepException
import org.ucop.domain.UseCaseLog
import kotlin.reflect.KClass

open class Actor(val name: Name)

open class PreCondition<T>(val validate: (T) -> Unit)
class EmptyPreCondition<T> : PreCondition<T>({ /* No validation */ })

// The preconditions of a use case, checked before its nominal scenario
data class PreConditions<T>(private val preConditions: List<PreCondition<T>>) {

    // PreConditions(condition1, condition2): the caller does not wrap the conditions in a list
    constructor(vararg preConditions: PreCondition<T>) : this(preConditions.toList())

    fun validate(t: T) = preConditions.forEach { it.validate(t) }

    companion object {
        fun <T> none() = PreConditions(EmptyPreCondition<T>())
    }
}

open class PostCondition<T>(val validate: (T) -> Unit)
class EmptyPostCondition<T> : PostCondition<T>({ /* No validation */ })

// The postconditions of a use case, checked after its nominal scenario
data class PostConditions<T>(private val postConditions: List<PostCondition<T>>) {

    // PostConditions(condition1, condition2): the caller does not wrap the conditions in a list
    constructor(vararg postConditions: PostCondition<T>) : this(postConditions.toList())

    fun validate(t: T) = postConditions.forEach { it.validate(t) }

    companion object {
        fun <T> none() = PostConditions(EmptyPostCondition<T>())
    }
}

data class Step<T>(val run: (T) -> Unit)

interface Scenario<T> {
    val steps: List<Step<T>>
    fun unfold(t: T) {
        steps.forEach { it.run(t) }
    }
}

data class NominalScenario<T>(override val steps: List<Step<T>>) : Scenario<T> {

    // NominalScenario(step1, step2): the caller does not wrap the steps in a list
    constructor(vararg steps: Step<T>) : this(steps.toList())

    override fun unfold(t: T) {
        try {
            steps.forEach { it.run(t) }
        } catch (se: StepException) {
            throw NominalException(se.errorMessage)
        }
    }
}

data class AlternativeScenario<T>(override val steps: List<Step<T>>) : Scenario<T> {

    constructor(vararg steps: Step<T>) : this(steps.toList())

    override fun unfold(t: T) {
        try {
            steps.forEach { it.run(t) }
        } catch (se: StepException) {
            throw AlternativeException(se.errorMessage)
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

    fun getPreConditions(): PreConditions<T>
    fun getNominalScenario(): NominalScenario<T>
    fun getPostConditions(): PostConditions<T>

    fun getAlternativeScenarii():Map<KClass<out NominalException>, AlternativeScenario<T>> = emptyMap()

    fun unfold(t: T) = UseCaseLog.execution(this::class.simpleName ?: "UseCase") {
        try {
            getPreConditions().validate(t)
            getNominalScenario().unfold(t)
            getPostConditions().validate(t)
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

    override fun getPreConditions(): PreConditions<T> = PreConditions.none()

    override fun getNominalScenario(): NominalScenario<T> = NominalScenario(listOf())

    override fun getPostConditions(): PostConditions<T> = PostConditions.none()

    override fun getAlternativeScenarii(): Map<KClass<out NominalException>, AlternativeScenario<T>> = emptyMap()

}

// The nominal scenario of a query: it answers the request, without changing anything
data class NominalQuery<T, R>(val answer: (T) -> R)

// A use case that answers a question (a read, a proposal) instead of changing the state: its preconditions are
// checked on the request, its nominal query gives the answer, and its postconditions are checked on the answer
interface QueryUseCase<T, R> {

    fun getPreConditions(): PreConditions<T>
    fun getNominalQuery(): NominalQuery<T, R>
    fun getPostConditions(): PostConditions<R>

    fun unfold(t: T): R = UseCaseLog.execution(this::class.simpleName ?: "QueryUseCase") {
        getPreConditions().validate(t)
        val answer = getNominalQuery().answer(t)
        getPostConditions().validate(answer)
        answer
    }
}
