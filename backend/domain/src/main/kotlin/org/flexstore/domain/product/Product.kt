package org.flexstore.domain.product

import org.flexstore.domain.sharedkernel.*
import org.ucop.domain.NominalException
import org.ucop.domain.NonEmptyString

sealed interface Product
data class DraftProduct(val name: Name,
                        val description: Description,
                        val category: Category,
                        val price: Price): Product
data class DefinedProduct(
    val productId: ProductId,
    val name: Name,
    val description: Description,
    val category: Category,
    val price: Price): Product {
    constructor(productId: ProductId, name: Name, price: Price) : this(productId, name, Description("No description"), Category.NOT_DEFINED, price)
}
data class ProductId(val id: Identity) {
    companion object {
        fun nextId() = ProductId(Identity.nextId())
    }
}

enum class Category { ELECTRONICS, BOOKS, CLOTHING, HOME, BEAUTY, TOYS, SPORTS, AUTOMOTIVE, GROCERY, HEALTH, ACCESSORIES, NOT_DEFINED }

data class ProductAlreadyExists(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
data class ProductCreationFailed(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
data class ProductNotFoundException(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
