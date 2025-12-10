package org.ucop.domain

data class StepException(val reason: Reason) : Exception(reason.value)
open class NominalException(open val reason: Reason) : Exception(reason.value)
data class PreConditionException(val reason: Reason) : Exception(reason.value)
data class PostConditionException(val reason: Reason) : Exception(reason.value)
data class AlternativeException(val reason: Reason) : Exception(reason.value)

data class Reason(val value: String) {
    init {
        if (value.isEmpty()) {
            throw IllegalArgumentException("Value must not be empty")
        }
    }
}
