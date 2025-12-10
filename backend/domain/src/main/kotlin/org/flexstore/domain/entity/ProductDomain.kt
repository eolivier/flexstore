package org.flexstore.domain.entity

import org.ucop.domain.NominalException
import org.ucop.domain.Reason

data class ProductAlreadyExists(override val reason: Reason) : NominalException(reason)
data class ProductCreationFailed(override val reason: Reason) : NominalException(reason)