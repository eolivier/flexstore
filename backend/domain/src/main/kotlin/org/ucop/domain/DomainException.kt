package org.ucop.domain

data class StepException(val errorMessage: ErrorMessage) : Exception(errorMessage.value)
open class NominalException(open val errorMessage: ErrorMessage) : Exception(errorMessage.value) {

    // A precondition not met, seen from outside the use case as a nominal failure: its cause is the precise
    // exception
    constructor(preConditionException: PreConditionException) : this(preConditionException.errorMessage) {
        initCause(preConditionException)
    }
}
// A precondition that is not met: the business exceptions of the preconditions derive from it
open class PreConditionException(open val errorMessage: ErrorMessage) : Exception(errorMessage.value)
data class PostConditionException(val errorMessage: ErrorMessage) : Exception(errorMessage.value)
data class AlternativeException(val errorMessage: ErrorMessage) : Exception(errorMessage.value)
