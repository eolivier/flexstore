package org.flexstore.domain.product

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*
import org.flexstore.domain.user.UserRepository
import org.flexstore.domain.user.model.UserId

// An administrator edits a product of the catalog: its SKU, its translations, its category and its price
data class UpdateProductRequest(val editor: UserId, val product: DefinedProduct)

class UpdateProductUseCase(
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
) : UseCase<UpdateProductRequest> {

    override fun getPreConditions(): PreConditions<UpdateProductRequest> = PreConditions(
        editorIsAdministratorCondition(),
        productExistsCondition(),
        skuIsWellFormedCondition(),
        skuIsFreeCondition()
    )

    // The administrator gives every translation of the product: a translation not given is removed
    override fun getNominalScenario(): NominalScenario<UpdateProductRequest> =
        NominalScenario(Step { request -> productRepository.save(request.product) })

    override fun getPostConditions(): PostConditions<UpdateProductRequest> = PostConditions.none()

    // The editor must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun editorIsAdministratorCondition() = PreCondition<UpdateProductRequest> { request ->
        if (userRepository.isNotAdministrator(request.editor)) {
            throw ProductAdministrationForbiddenException(ErrorMessage("Only an administrator can change a product."))
        }
    }

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
