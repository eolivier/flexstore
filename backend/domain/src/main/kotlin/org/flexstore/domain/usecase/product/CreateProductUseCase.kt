package org.flexstore.domain.usecase.product

import org.flexstore.domain.entity.*
import org.flexstore.domain.repository.ProductRepository
import org.flexstore.domain.valueobject.DefinedProduct
import org.flexstore.domain.valueobject.DraftProduct
import org.flexstore.domain.valueobject.ProductId
import org.ucop.domain.NominalException
import org.ucop.domain.NonEmptyString
import org.ucop.domain.entity.*
import kotlin.reflect.KClass

// The id of the new product is chosen by the caller, which can then read the created product
data class CreateProductRequest(val productId: ProductId, val draftProduct: DraftProduct) {
    fun definedProduct() = DefinedProduct(
        productId = productId,
        name = draftProduct.name,
        description = draftProduct.description,
        category = draftProduct.category,
        price = draftProduct.price
    )
}

class CreateProductUseCase(private val productRepository: ProductRepository) : UseCase<CreateProductRequest> {

    override fun getPreConditions(): List<PreCondition<CreateProductRequest>> = listOf(productDoesNotExistCondition())

    override fun getNominalScenario(): NominalScenario<CreateProductRequest> {
        val createProductStep = Step<CreateProductRequest> { request -> productRepository.save(request.definedProduct()) }
        return NominalScenario(listOf(createProductStep))
    }

    override fun getPostConditions(): List<PostCondition<CreateProductRequest>> = listOf(productExistsCondition())

    override fun getAlternativeScenarii(): Map<KClass<out NominalException>, AlternativeScenario<CreateProductRequest>> {
        return emptyMap()
    }

    private fun productDoesNotExistCondition() = PreCondition<CreateProductRequest> { request ->
        if (productRepository.exists(request.draftProduct)) {
            throw ProductAlreadyExists(NonEmptyString("Product ${request.draftProduct} already exists."))
        }
    }

    private fun productExistsCondition() = PostCondition<CreateProductRequest> { request ->
        if (productRepository.findById(request.productId) == null) {
            throw ProductCreationFailed(NonEmptyString("Product ${request.draftProduct} was not created."))
        }
    }
}
