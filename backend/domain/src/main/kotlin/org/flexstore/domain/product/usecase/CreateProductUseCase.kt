package org.flexstore.domain.product.usecase

import kotlin.reflect.KClass
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.actor.isNotAdmin
import org.ucop.domain.entity.Actor
import org.ucop.domain.ErrorMessage
import org.ucop.domain.NominalException
import org.ucop.domain.entity.*

// An administrator creates a product. The id of the new product is chosen by the caller, which can then read the
// created product
data class CreateProductRequest(val creator: Actor, val productId: ProductId, val draftProduct: DraftProduct) {
    fun definedProduct() = DefinedProduct(
        productId,
        draftProduct.sku,
        draftProduct.translations,
        draftProduct.category,
        draftProduct.pricing
    )
}

class CreateProductUseCase(
    private val productRepository: ProductRepository
) : UseCase<CreateProductRequest> {

    override fun getPreConditions(): PreConditions<CreateProductRequest> = PreConditions(
        creatorIsAdministratorCondition(),
        productDoesNotExistCondition(),
        skuIsWellFormedCondition(),
        skuIsFreeCondition(),
        promotionIsValidCondition()
    )

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

    // The creator, resolved before the use case runs, must be an Admin
    private fun creatorIsAdministratorCondition() = PreCondition<CreateProductRequest> { request ->
        if (request.creator.isNotAdmin()) {
            val message = ErrorMessage("Only an administrator can create a product.")
            throw ProductAdministrationForbiddenException(message)
        }
    }

    private fun skuIsWellFormedCondition() = PreCondition<CreateProductRequest> { request ->
        throwIfMalformed(request.draftProduct.sku)
    }

    private fun skuIsFreeCondition() = PreCondition<CreateProductRequest> { request ->
        throwIfUsedByAnotherProduct(request.definedProduct(), productRepository)
    }

    private fun promotionIsValidCondition() = PreCondition<CreateProductRequest> { request ->
        throwIfInvalid(request.draftProduct.pricing.promotion)
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
