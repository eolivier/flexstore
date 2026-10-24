package org.ucop.domain

import org.flexstore.domain.sharedkernel.ErrorMessage

data class StepException(val errorMessage: ErrorMessage) : Exception(errorMessage.value)
open class NominalException(open val errorMessage: ErrorMessage) : Exception(errorMessage.value)
data class PreConditionException(val errorMessage: ErrorMessage) : Exception(errorMessage.value)
data class PostConditionException(val errorMessage: ErrorMessage) : Exception(errorMessage.value)
data class AlternativeException(val errorMessage: ErrorMessage) : Exception(errorMessage.value)
