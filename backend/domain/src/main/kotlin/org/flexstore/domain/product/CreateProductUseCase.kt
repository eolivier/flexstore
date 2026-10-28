package org.flexstore.domain.product

import org.ucop.domain.NominalException
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*
import kotlin.reflect.KClass

// The id of the new product is chosen by the caller, which can then read the created product
data class CreateProductRequest(val productId: ProductId, val draftProduct: DraftProduct) {
    fun definedProduct() =
        DefinedProduct(productId, draftProduct.translations, draftProduct.category, draftProduct.price)
}

class CreateProductUseCase(private val productRepository: ProductRepository) : UseCase<CreateProductRequest> {

    override fun getPreConditions(): PreConditions<CreateProductRequest> =
        PreConditions(productDoesNotExistCondition())

    override fun getNominalScenario(): NominalScenario<CreateProductRequest> {
        val createProductStep = Step<CreateProductRequest> { request ->
            productRepository.save(request.definedProduct())
        }
        return NominalScenario(listOf(createProductStep))
    }

    override fun getPostConditions(): PostConditions<CreateProductRequest> =
        PostConditions(productExistsCondition())

    override fun getAlternativeScenarii():
        Map<KClass<out NominalException>, AlternativeScenario<CreateProductRequest>> {
        return emptyMap()
    }

    private fun productDoesNotExistCondition() = PreCondition<CreateProductRequest> { request ->
        if (productRepository.exists(request.draftProduct)) {
            throw ProductAlreadyExists(ErrorMessage("Product ${request.draftProduct} already exists."))
        }
    }

    private fun productExistsCondition() = PostCondition<CreateProductRequest> { request ->
        if (productRepository.productIsMissing(request.productId)) {
            throw ProductCreationFailed(ErrorMessage("Product ${request.draftProduct} was not created."))
        }
    }
}
