package org.flexstore.domain.wishlist.usecase

import org.flexstore.domain.product.ProductNotFoundException
import org.flexstore.domain.product.ProductRepository
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.UserRepository
import org.flexstore.domain.user.model.AccountRefusedException
import org.flexstore.domain.user.model.UserNotFoundException
import org.flexstore.domain.wishlist.*
import org.ucop.domain.entity.*

// A customer saves a product to their wishlist, to buy it back later: a product already saved stays as it is
class AddProductToWishlistUseCase(
    private val wishlistRepository: WishlistRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
) : UseCase<WishlistEntry> {

    override fun getPreConditions(): PreConditions<WishlistEntry> =
        PreConditions(customerCanActCondition(), productExistsCondition())

    override fun getNominalScenario(): NominalScenario<WishlistEntry> {
        val saveProductStep = Step<WishlistEntry> { entry ->
            if (wishlistRepository.doesNotContain(entry.customer, entry.productId)) {
                val wishlistItem = WishlistItem(WishlistItemId.nextId(), entry.customer, entry.productId,
                    SavedAt.now())
                wishlistRepository.save(wishlistItem)
            }
        }
        return NominalScenario(saveProductStep)
    }

    override fun getPostConditions(): PostConditions<WishlistEntry> = PostConditions(productIsSavedCondition())

    // The customer must be an existing user whose account is not suspended, locked or disabled (read
    // from the users, not from the request)
    private fun customerCanActCondition() = PreCondition<WishlistEntry> { entry ->
        val customer = entry.customer
        if (userRepository.notExists(customer)) {
            throw UserNotFoundException(ErrorMessage("User ${customer.value} not found."))
        }
        val status = userRepository.statusOf(customer)
        if (status.refusesSignIn()) {
            val message = ErrorMessage("The account ${customer.value} is $status: it cannot act.")
            throw AccountRefusedException(status, message)
        }
    }

    private fun productExistsCondition() = PreCondition<WishlistEntry> { entry ->
        if (productRepository.productIsMissing(entry.productId)) {
            throw ProductNotFoundException(ErrorMessage("The product ${entry.productId.id.value} does not exist."))
        }
    }

    private fun productIsSavedCondition() = PostCondition<WishlistEntry> { entry ->
        if (wishlistRepository.doesNotContain(entry.customer, entry.productId)) {
            val message = ErrorMessage("Product ${entry.productId.id.value} was not saved to the wishlist.")
            throw WishlistUpdateFailed(message)
        }
    }
}
