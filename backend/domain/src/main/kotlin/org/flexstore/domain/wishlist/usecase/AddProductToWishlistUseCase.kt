package org.flexstore.domain.wishlist.usecase

import org.flexstore.domain.product.model.ProductNotFoundException
import org.flexstore.domain.product.ProductRepository
import org.flexstore.domain.actor.customerId
import org.flexstore.domain.actor.verifyCanAct
import org.flexstore.domain.wishlist.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A customer saves a product to their wishlist, to buy it back later: a product already saved stays as it is
class AddProductToWishlistUseCase(
    private val wishlistRepository: WishlistRepository,
    private val productRepository: ProductRepository
) : UseCase<WishlistEntry> {

    override fun getPreConditions(): PreConditions<WishlistEntry> =
        PreConditions(customerCanActCondition(), productExistsCondition())

    override fun getNominalScenario(): NominalScenario<WishlistEntry> {
        val saveProductStep = Step<WishlistEntry> { entry ->
            if (wishlistRepository.doesNotContain(entry.customer.customerId(), entry.productId)) {
                val wishlistItem = WishlistItem(WishlistItemId.nextId(), entry.customer.customerId(),
                    entry.productId, SavedAt.now())
                wishlistRepository.save(wishlistItem)
            }
        }
        return NominalScenario(saveProductStep)
    }

    override fun getPostConditions(): PostConditions<WishlistEntry> = PostConditions(productIsSavedCondition())

    // The customer, resolved before the use case runs, must be a customer or an admin who can act
    private fun customerCanActCondition() = PreCondition<WishlistEntry> { entry ->
        entry.customer.verifyCanAct()
    }

    private fun productExistsCondition() = PreCondition<WishlistEntry> { entry ->
        if (productRepository.productIsMissing(entry.productId)) {
            throw ProductNotFoundException(ErrorMessage("The product ${entry.productId.id.value} does not exist."))
        }
    }

    private fun productIsSavedCondition() = PostCondition<WishlistEntry> { entry ->
        if (wishlistRepository.doesNotContain(entry.customer.customerId(), entry.productId)) {
            val message = ErrorMessage("Product ${entry.productId.id.value} was not saved to the wishlist.")
            throw WishlistUpdateFailed(message)
        }
    }
}
