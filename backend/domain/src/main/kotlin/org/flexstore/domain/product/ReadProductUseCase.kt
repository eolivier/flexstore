package org.flexstore.domain.product

import org.ucop.domain.NominalException
import org.flexstore.domain.sharedkernel.ErrorMessage

class ReadProductUseCase(private val productRepository: ProductRepository) {
    fun getProducts() = productRepository.findAll()

    fun getProduct(productId: ProductId): DefinedProduct =
        productRepository.findById(productId)
            ?: throw ProductNotFoundException(ErrorMessage("Product with ID ${productId.id.value} does not exist."))
}

