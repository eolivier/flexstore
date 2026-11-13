package org.ucop.domain

open class NominalException(open val errorMessage: ErrorMessage) : Exception(errorMessage.value) {

    // A precondition not met, seen from outside the use case as a nominal failure: its cause is the precise
    // exception
    constructor(preConditionException: PreConditionException) : this(preConditionException.errorMessage) {
        initCause(preConditionException)
    }

    // The same for a postcondition not met
    constructor(postConditionException: PostConditionException) : this(postConditionException.errorMessage) {
        initCause(postConditionException)
    }
}
// A precondition that is not met: the business exceptions of the preconditions derive from it
open class PreConditionException(open val errorMessage: ErrorMessage) : Exception(errorMessage.value)
// A postcondition that is not met: the business exceptions of the postconditions derive from it
open class PostConditionException(open val errorMessage: ErrorMessage) : Exception(errorMessage.value)

fun Throwable.isNotNominal() = this !is NominalException
