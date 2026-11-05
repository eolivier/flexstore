package org.flexstore.domain.image.usecase

import org.flexstore.domain.image.*
import org.flexstore.domain.user.repository.UserRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator removes the image of a product, in every format
class RemoveProductImageUseCase(
    private val productImageRepository: ProductImageRepository,
    private val userRepository: UserRepository
) : UseCase<ProductImageRemoval> {

    override fun getPreConditions(): PreConditions<ProductImageRemoval> =
        PreConditions(editorIsAdministratorCondition(), productHasImageCondition())

    override fun getNominalScenario(): NominalScenario<ProductImageRemoval> =
        NominalScenario(Step { (_, productId) -> productImageRepository.deleteAllOf(productId) })

    override fun getPostConditions(): PostConditions<ProductImageRemoval> =
        PostConditions(imageIsRemovedCondition())

    // The editor must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun editorIsAdministratorCondition() = PreCondition<ProductImageRemoval> { request ->
        if (userRepository.isNotAdministrator(request.editor)) {
            throw ProductImageForbiddenException(ErrorMessage("Only an administrator can remove the image of a product."))
        }
    }

    private fun productHasImageCondition() = PreCondition<ProductImageRemoval> { (_, productId) ->
        if (productId !in productImageRepository.productsWithImage()) {
            throw ProductImageNotFoundException(ErrorMessage("Product ${productId.id.value} has no image."))
        }
    }

    private fun imageIsRemovedCondition() = PostCondition<ProductImageRemoval> { (_, productId) ->
        if (productId in productImageRepository.productsWithImage()) {
            throw ProductImageStorageFailed(ErrorMessage("The image of ${productId.id.value} was not removed."))
        }
    }
}
