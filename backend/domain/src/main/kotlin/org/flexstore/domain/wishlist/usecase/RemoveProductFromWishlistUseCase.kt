package org.flexstore.domain.wishlist.usecase

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.UserRepository
import org.flexstore.domain.wishlist.*
import org.ucop.domain.entity.*

// A customer removes a product from their wishlist: a product not saved stays absent
class RemoveProductFromWishlistUseCase(
    private val wishlistRepository: WishlistRepository,
    private val userRepository: UserRepository
) : UseCase<WishlistEntry> {

    override fun getPreConditions(): PreConditions<WishlistEntry> = PreConditions(customerCanActCondition())

    override fun getNominalScenario(): NominalScenario<WishlistEntry> =
        NominalScenario(Step { entry -> wishlistRepository.remove(entry.customer, entry.productId) })

    override fun getPostConditions(): PostConditions<WishlistEntry> = PostConditions(productIsRemovedCondition())

    private fun customerCanActCondition() = PreCondition<WishlistEntry> { entry ->
        userRepository.verifyCustomerCanAct(entry.customer)
    }

    private fun productIsRemovedCondition() = PostCondition<WishlistEntry> { entry ->
        if (wishlistRepository.contains(entry.customer, entry.productId)) {
            val message = ErrorMessage("Product ${entry.productId.id.value} was not removed from the wishlist.")
            throw WishlistUpdateFailed(message)
        }
    }
}
