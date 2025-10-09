package org.flexstore.domain.entity

import org.ucop.domain.NominalException
import org.ucop.domain.NonEmptyString

data class ProductAlreadyExists(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
data class ProductCreationFailed(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)