package org.flexstore.domain.cart.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.UserRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A quantity of zero or less removes the item; an item of another cart is unknown to the owner's cart, hence
// ignored
data class ChangeCartItemQuantityRequest(val owner: CartOwner, val itemId: ItemId, val quantity: Quantity)

class ChangeCartItemQuantityUseCase(
    private val cartRepository: CartRepository,
    private val userRepository: UserRepository
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

    // A customer owner must be an existing user whose account is not suspended, locked or disabled
    // (read from the users, not from the request); a visitor has none
    private fun ownerCanActCondition() = PreCondition<ChangeCartItemQuantityRequest> { request ->
        val owner = request.owner
        if (owner is CartOwner.Customer) {
            userRepository.verifyCustomerCanAct(owner.userId)
        }
    }

    private fun quantityChangedCondition() = PostCondition<ChangeCartItemQuantityRequest> { request ->
        val item = cartRepository.load(request.owner).findBy(request.itemId)
        // An item kept with another quantity than requested, or kept when it should have been removed
        val quantityStillThere = request.quantity.isZeroOrNegative() || item?.quantity != request.quantity
        val quantityNotApplied = item != null && quantityStillThere
        if (quantityNotApplied) {
            throw CartUpdateFailed(ErrorMessage("Quantity of item ${request.itemId.id.value} was not changed."))
        }
    }
}
