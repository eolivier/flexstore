package org.flexstore.domain.product.usecase

import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.actor.isNotAdmin
import org.ucop.domain.entity.Actor
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator edits a product of the catalog: its SKU, its translations, its category, its price and its
// promotion
data class UpdateProductRequest(val editor: Actor, val product: DefinedProduct)

class UpdateProductUseCase(
    private val productRepository: ProductRepository
) : UseCase<UpdateProductRequest> {

    override fun getPreConditions(): PreConditions<UpdateProductRequest> = PreConditions(
        editorIsAdministratorCondition(),
        productExistsCondition(),
        skuIsWellFormedCondition(),
        skuIsFreeCondition(),
        promotionIsValidCondition()
    )

    // The administrator gives every translation of the product: a translation not given is removed
    override fun getNominalScenario(): NominalScenario<UpdateProductRequest> =
        NominalScenario(Step { request -> productRepository.save(request.product) })

    override fun getPostConditions(): PostConditions<UpdateProductRequest> = PostConditions.none()

    // The editor, resolved before the use case runs, must be an Admin
    private fun editorIsAdministratorCondition() = PreCondition<UpdateProductRequest> { request ->
        if (request.editor.isNotAdmin()) {
            val message = ErrorMessage("Only an administrator can change a product.")
            throw ProductAdministrationForbiddenException(message)
        }
    }

    private fun productExistsCondition() = PreCondition<UpdateProductRequest> { request ->
        if (productRepository.productIsMissing(request.product.productId)) {
            val message = ErrorMessage("Product with ID ${request.product.productId.id.value} does not exist.")
            throw ProductNotFoundException(message)
        }
    }

    private fun promotionIsValidCondition() = PreCondition<UpdateProductRequest> { request ->
        throwIfInvalid(request.product.promotion)
    }

    private fun skuIsWellFormedCondition() = PreCondition<UpdateProductRequest> { request ->
        throwIfMalformed(request.product.sku)
    }

    private fun skuIsFreeCondition() = PreCondition<UpdateProductRequest> { request ->
        throwIfUsedByAnotherProduct(request.product, productRepository)
    }
}
