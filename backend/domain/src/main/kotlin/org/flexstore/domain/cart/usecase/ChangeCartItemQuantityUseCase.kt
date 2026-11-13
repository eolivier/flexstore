package org.flexstore.domain.cart.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.sharedkernel.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A quantity of zero or less removes the item; an item of another cart is unknown to the owner's cart, hence
// ignored
data class ChangeCartItemQuantityRequest(
    val actor: Actor,
    val owner: CartOwner,
    val itemId: ItemId,
    val quantity: Quantity
)

class ChangeCartItemQuantityUseCase(
    private val cartRepository: CartRepository
) : UseCase<ChangeCartItemQuantityRequest> {

    override fun getPreConditions(): PreConditions<ChangeCartItemQuantityRequest> =
        PreConditions(ownerCanActCondition())

    override fun getNominalScenario(): NominalScenario<ChangeCartItemQuantityRequest> {
        val changeQuantityStep = Step<ChangeCartItemQuantityRequest> { request ->
            val cart = cartRepository.load(request.owner)
            cartRepository.save(cart.changeQuantity(request.itemId, request.quantity))
        }
        return NominalScenario(listOf(changeQuantityStep))
    }

    override fun getPostConditions(): PostConditions<ChangeCartItemQuantityRequest> =
        PostConditions(quantityChangedCondition())

    // The actor, resolved before the use case runs, must be able to act when the cart is the one of a customer
    private fun ownerCanActCondition() = PreCondition<ChangeCartItemQuantityRequest> { request ->
        request.actor.verifyCanUseCartOf(request.owner)
    }

    private fun quantityChangedCondition() = PostCondition<ChangeCartItemQuantityRequest> { request ->
        val item = cartRepository.load(request.owner).findBy(request.itemId)
        // An item kept with another quantity than requested, or kept when it should have been removed
        val quantityStillThere = request.quantity.isZeroOrNegative() || item?.quantity != request.quantity
        val quantityNotApplied = item != null && quantityStillThere
        if (quantityNotApplied) {
            throw CartUpdateFailed(ErrorMessage("Quantity of item ${request.itemId.value} was not changed."))
        }
    }
}
