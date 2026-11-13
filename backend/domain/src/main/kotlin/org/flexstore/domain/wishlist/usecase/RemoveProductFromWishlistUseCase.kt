package org.flexstore.domain.wishlist.usecase

import org.flexstore.domain.actor.customerId
import org.flexstore.domain.actor.verifyCanAct
import org.flexstore.domain.wishlist.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A customer removes a product from their wishlist: a product not saved stays absent
class RemoveProductFromWishlistUseCase(
    private val wishlistRepository: WishlistRepository
) : UseCase<WishlistEntry> {

    override fun getPreConditions(): PreConditions<WishlistEntry> = PreConditions(customerCanActCondition())

    override fun getNominalScenario(): NominalScenario<WishlistEntry> =
        NominalScenario(Step { entry ->
            wishlistRepository.remove(entry.customer.customerId(), entry.productId)
        })

    override fun getPostConditions(): PostConditions<WishlistEntry> = PostConditions(productIsRemovedCondition())

    // The customer, resolved before the use case runs, must be a customer or an admin who can act
    private fun customerCanActCondition() = PreCondition<WishlistEntry> { entry ->
        entry.customer.verifyCanAct()
    }

    private fun productIsRemovedCondition() = PostCondition<WishlistEntry> { entry ->
        if (wishlistRepository.contains(entry.customer.customerId(), entry.productId)) {
            val message = ErrorMessage("Product ${entry.productId.asString()} was not removed from the wishlist.")
            throw WishlistUpdateFailed(message)
        }
    }
}
