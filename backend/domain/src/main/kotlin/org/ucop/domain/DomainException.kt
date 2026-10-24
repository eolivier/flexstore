package org.ucop.domain

data class StepException(val errorMessage: ErrorMessage) : Exception(errorMessage.value)
open class NominalException(open val errorMessage: ErrorMessage) : Exception(errorMessage.value)
data class PreConditionException(val errorMessage: ErrorMessage) : Exception(errorMessage.value)
data class PostConditionException(val errorMessage: ErrorMessage) : Exception(errorMessage.value)
data class AlternativeException(val errorMessage: ErrorMessage) : Exception(errorMessage.value)

// The message of a domain exception: never empty, so that every failure says what happened
data class ErrorMessage(val value: String) {
    init {
        require(value.isNotEmpty()) { "An error message must not be empty" }
    }
}
