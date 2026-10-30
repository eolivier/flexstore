package org.flexstore.domain.product

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*

// A product of the catalog
class ReadProductUseCase(
    private val productRepository: ProductRepository
) : QueryUseCase<ProductId, DefinedProduct> {

    override fun getPreConditions(): PreConditions<ProductId> = PreConditions(productExistsCondition())

    override fun getNominalQuery() = NominalQuery<ProductId, DefinedProduct> { productId ->
        productRepository.findById(productId)!!
    }

    override fun getPostConditions(): PostConditions<DefinedProduct> = PostConditions.none()

    private fun productExistsCondition() = PreCondition<ProductId> { productId ->
        if (productRepository.productIsMissing(productId)) {
            throw ProductNotFoundException(ErrorMessage("Product with ID ${productId.id.value} does not exist."))
        }
    }
}
