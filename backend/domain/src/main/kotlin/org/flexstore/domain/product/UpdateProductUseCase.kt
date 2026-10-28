package org.flexstore.domain.product

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*

// An administrator edits a product of the catalog: its SKU, its translations, its category and its price
data class UpdateProductRequest(val product: DefinedProduct)

class UpdateProductUseCase(private val productRepository: ProductRepository) : UseCase<UpdateProductRequest> {

    override fun getPreConditions(): PreConditions<UpdateProductRequest> =
        PreConditions(productExistsCondition(), skuIsWellFormedCondition(), skuIsFreeCondition())

    // The administrator gives every translation of the product: a translation not given is removed
    override fun getNominalScenario(): NominalScenario<UpdateProductRequest> =
        NominalScenario(Step { request -> productRepository.save(request.product) })

    override fun getPostConditions(): PostConditions<UpdateProductRequest> = PostConditions.none()

    private fun productExistsCondition() = PreCondition<UpdateProductRequest> { request ->
        if (productRepository.productIsMissing(request.product.productId)) {
            val message = ErrorMessage("Product with ID ${request.product.productId.id.value} does not exist.")
            throw ProductNotFoundException(message)
        }
    }

    private fun skuIsWellFormedCondition() = PreCondition<UpdateProductRequest> { request ->
        throwIfMalformed(request.product.sku)
    }

    private fun skuIsFreeCondition() = PreCondition<UpdateProductRequest> { request ->
        throwIfUsedByAnotherProduct(request.product, productRepository)
    }
}
