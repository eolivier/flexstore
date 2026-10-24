package org.flexstore.domain.product

import org.ucop.domain.NominalException
import org.ucop.domain.NonEmptyString

class ReadProductUseCase(private val productRepository: ProductRepository) {
    fun getProducts() = productRepository.findAll()

    fun getProduct(productId: ProductId): DefinedProduct =
        productRepository.findById(productId)
            ?: throw ProductNotFoundException(NonEmptyString("Product with ID ${productId.id.value} does not exist."))
}

