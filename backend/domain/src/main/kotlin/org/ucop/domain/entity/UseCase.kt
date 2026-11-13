package org.ucop.domain.entity

import org.flexstore.domain.sharedkernel.*
import org.ucop.domain.NominalException
import org.ucop.domain.PostConditionException
import org.ucop.domain.PreConditionException
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

    // What a use case runs: a precondition not met is raised as a nominal failure, caused by the precise
    // exception
    fun validateOrFail(t: T) {
        try {
            validate(t)
        } catch (pce: PreConditionException) {
            throw NominalException(pce)
        }
    }

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

    // What a use case runs: a postcondition not met is raised as a nominal failure, caused by the precise
    // exception
    fun validateOrFail(t: T) {
        try {
            validate(t)
        } catch (pce: PostConditionException) {
            throw NominalException(pce)
        }
    }

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
}

data class AlternativeScenario<T>(override val steps: List<Step<T>>) : Scenario<T> {

    constructor(vararg steps: Step<T>) : this(steps.toList())
}

interface UseCase<T> {

    fun getPreConditions(): PreConditions<T>
    fun getNominalScenario(): NominalScenario<T>
    fun getPostConditions(): PostConditions<T>

    fun getAlternativeScenarii():Map<KClass<out NominalException>, AlternativeScenario<T>> = emptyMap()

    fun unfold(t: T) = UseCaseLog.execution(this::class.simpleName ?: "UseCase") {
        try {
            getPreConditions().validateOrFail(t)
            getNominalScenario().unfold(t)
            getPostConditions().validateOrFail(t)
        } catch (ne: NominalException) {
            if (getAlternativeScenarii().containsKey(ne::class)) {
                getAlternativeScenarii()[ne::class]?.unfold(t)
            } else {
                throw ne
            }
        }
    }
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
        getPreConditions().validateOrFail(t)
        val answer = getNominalQuery().answer(t)
        getPostConditions().validateOrFail(answer)
        answer
    }
}
