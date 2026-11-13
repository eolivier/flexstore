package org.flexstore.domain.cart.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.PostConditionException
import org.ucop.domain.entity.*

data class AddItemToCartRequest(
    val actor: Actor,
    val owner: CartOwner,
    val productId: ProductId,
    val quantity: Quantity
)

class AddItemToCartUseCase(
    private val cartRepository: CartRepository,
    private val productRepository: ProductRepository
) : UseCase<AddItemToCartRequest> {

    override fun getPreConditions(): PreConditions<AddItemToCartRequest> =
        PreConditions(ownerCanActCondition(), productExistsCondition())

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
            ?: throw ProductNotFoundException(ErrorMessage("Product ${productId.value} not found."))

    // The actor, resolved before the use case runs, must be able to act when the cart is the one of a customer
    private fun ownerCanActCondition() = PreCondition<AddItemToCartRequest> { request ->
        request.actor.verifyCanUseCartOf(request.owner)
    }

    private fun productExistsCondition() = PreCondition<AddItemToCartRequest> { request ->
        catalogProduct(request.productId)
    }

    private fun itemHasQuantityCondition() = PostCondition<AddItemToCartRequest> { request ->
        val item = cartRepository.load(request.owner).findBy(request.productId)
        if (item is CartItem && item.quantity == request.quantity) return@PostCondition
        val message = ErrorMessage("Product ${request.productId.value} was not added to the cart.")
        throw CartUpdateFailed(message)
    }
}

data class CartUpdateFailed(override val errorMessage: ErrorMessage) : PostConditionException(errorMessage)
