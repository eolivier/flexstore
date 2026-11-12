package org.ucop.domain

data class StepException(val errorMessage: ErrorMessage) : Exception(errorMessage.value)
open class NominalException(open val errorMessage: ErrorMessage) : Exception(errorMessage.value)
// A precondition that is not met: the business exceptions of the preconditions derive from it
open class PreConditionException(open val errorMessage: ErrorMessage) : Exception(errorMessage.value)

// Seen from outside the use case, a precondition not met is a nominal failure; its cause is the precise
// exception
class PreConditionFailed(override val cause: PreConditionException) : NominalException(cause.errorMessage)
data class PostConditionException(val errorMessage: ErrorMessage) : Exception(errorMessage.value)
data class AlternativeException(val errorMessage: ErrorMessage) : Exception(errorMessage.value)
