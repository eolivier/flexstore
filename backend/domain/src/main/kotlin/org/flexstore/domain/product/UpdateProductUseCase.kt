package org.flexstore.domain.product

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*

// An administrator edits a product of the catalog: name, description, category and price
data class UpdateProductRequest(val product: DefinedProduct)

class UpdateProductUseCase(private val productRepository: ProductRepository) : UseCase<UpdateProductRequest> {

    override fun getPreConditions(): PreConditions<UpdateProductRequest> =
        PreConditions(productExistsCondition())

    // The product is edited in the default language: its translations in the other languages are kept
    override fun getNominalScenario(): NominalScenario<UpdateProductRequest> =
        NominalScenario(Step { request ->
            val storedProduct = productRepository.findById(request.product.productId)!!
            val editedProduct = request.product.keepingTranslationsOf(storedProduct)
            productRepository.save(editedProduct)
        })

    override fun getPostConditions(): PostConditions<UpdateProductRequest> = PostConditions.none()

    private fun productExistsCondition() = PreCondition<UpdateProductRequest> { request ->
        if (productRepository.productIsMissing(request.product.productId)) {
            val message = ErrorMessage("Product with ID ${request.product.productId.id.value} does not exist.")
            throw ProductNotFoundException(message)
        }
    }
}
