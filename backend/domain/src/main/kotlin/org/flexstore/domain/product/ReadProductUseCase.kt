package org.flexstore.domain.product

import org.ucop.domain.NominalException
import org.ucop.domain.UseCaseLog
import org.flexstore.domain.sharedkernel.ErrorMessage

class ReadProductUseCase(private val productRepository: ProductRepository) {
    fun getProducts() = UseCaseLog.execution("ReadProductUseCase.getProducts") { productRepository.findAll() }

    fun getProduct(productId: ProductId): DefinedProduct = UseCaseLog.execution("ReadProductUseCase.getProduct") {
        productRepository.findById(productId)
            ?: throw ProductNotFoundException(ErrorMessage("Product with ID ${productId.id.value} does not exist."))
    }
}

