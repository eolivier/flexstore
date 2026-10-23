package org.flexstore.domain.usecase.product

import org.flexstore.domain.repository.ProductRepository
import org.flexstore.domain.valueobject.DefinedProduct
import org.flexstore.domain.valueobject.ProductId
import org.ucop.domain.NominalException
import org.ucop.domain.NonEmptyString

class ReadProductUseCase(private val productRepository: ProductRepository) {
    fun getProducts() = productRepository.findAll()

    fun getProduct(productId: ProductId): DefinedProduct =
        productRepository.findById(productId)
            ?: throw ProductNotFoundException(NonEmptyString("Product with ID ${productId.id.value} does not exist."))
}

data class ProductNotFoundException(override val nonEmptyMessage: NonEmptyString) : NominalException(nonEmptyMessage)
