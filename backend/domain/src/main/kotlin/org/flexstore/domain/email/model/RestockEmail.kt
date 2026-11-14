package org.flexstore.domain.email.model

import org.flexstore.domain.product.model.DefinedProduct
import org.flexstore.domain.user.model.*

// The email that tells a customer that a product they waited for is back in stock; its wording is
// RestockEmailContent
data class RestockEmail(val recipient: Email, val customerName: FullName, val product: DefinedProduct)
