package org.flexstore.domain.image.usecase

import org.flexstore.domain.image.*
import org.flexstore.domain.product.ProductId
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*

// An administrator removes the image of a product, in every format
class RemoveProductImageUseCase(private val productImageRepository: ProductImageRepository) :
    UseCase<ProductId> {

    override fun getPreConditions(): PreConditions<ProductId> = PreConditions(productHasImageCondition())

    override fun getNominalScenario(): NominalScenario<ProductId> =
        NominalScenario(Step { productId -> productImageRepository.deleteAllOf(productId) })

    override fun getPostConditions(): PostConditions<ProductId> = PostConditions(imageIsRemovedCondition())

    private fun productHasImageCondition() = PreCondition<ProductId> { productId ->
        if (productId !in productImageRepository.productsWithImage()) {
            throw ProductImageNotFoundException(ErrorMessage("Product ${productId.id.value} has no image."))
        }
    }

    private fun imageIsRemovedCondition() = PostCondition<ProductId> { productId ->
        if (productId in productImageRepository.productsWithImage()) {
            throw ProductImageStorageFailed(ErrorMessage("The image of ${productId.id.value} was not removed."))
        }
    }
}
