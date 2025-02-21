package org.flexstore.domain

data class StepException(val nonEmptyMessage: NonEmptyString) : Exception(nonEmptyMessage.value)
data class NominalException(val nonEmptyMessage: NonEmptyString) : Exception(nonEmptyMessage.value)
data class PreConditionException(val nonEmptyMessage: NonEmptyString) : Exception(nonEmptyMessage.value)
data class PostConditionException(val nonEmptyMessage: NonEmptyString) : Exception(nonEmptyMessage.value)
data class AlternativeException(val nonEmptyMessage: NonEmptyString) : Exception(nonEmptyMessage.value)

data class NonEmptyString(val value: String) {
    init {
        if (value.isEmpty()) {
            throw IllegalArgumentException("Value must not be empty")
        }
    }
}
