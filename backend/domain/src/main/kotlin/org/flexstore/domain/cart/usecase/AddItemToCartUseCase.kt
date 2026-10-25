package org.flexstore.domain.cart.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.ucop.domain.NominalException
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*

data class AddItemToCartRequest(val owner: CartOwner, val productId: ProductId, val quantity: Quantity)

class AddItemToCartUseCase(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository
) : UseCase<AddItemToCartRequest> {

    override fun getPreConditions(): PreConditions<AddItemToCartRequest> =
        PreConditions(productExistsCondition())

    override fun getNominalScenario(): NominalScenario<AddItemToCartRequest> {
        val addItemStep = Step<AddItemToCartRequest> { request ->
            val cart = cartRepository.load(request.owner)
            cartRepository.save(cart.add(catalogProduct(request.productId), request.quantity))
        }
        return NominalScenario(listOf(addItemStep))
    }

    override fun getPostConditions(): PostConditions<AddItemToCartRequest> =
        PostConditions(itemHasQuantityCondition())

    private fun catalogProduct(productId: ProductId): DefinedProduct =
        productRepository.findById(productId)
            ?: throw ProductNotFoundException(ErrorMessage("Product ${productId.id.value} not found."))

    private fun productExistsCondition() = PreCondition<AddItemToCartRequest> { request ->
        catalogProduct(request.productId)
    }

    private fun itemHasQuantityCondition() = PostCondition<AddItemToCartRequest> { request ->
        val item = cartRepository.load(request.owner).findBy(request.productId)
        if (item !is CartItem || item.quantity != request.quantity) {
            val message = ErrorMessage("Product ${request.productId.id.value} was not added to the cart.")
            throw CartUpdateFailed(message)
        }
    }
}

data class CartUpdateFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
