package org.flexstore.domain.wishlist.usecase

import org.flexstore.domain.product.Products
import org.flexstore.domain.user.UserRepository
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.wishlist.WishlistRepository
import org.ucop.domain.entity.*

// The products a customer saved to their wishlist, newest first, to render them like the catalog
class ListWishlistUseCase(
    private val wishlistRepository: WishlistRepository,
    private val userRepository: UserRepository
) : QueryUseCase<UserId.ValidUserId, Products> {

    override fun getPreConditions(): PreConditions<UserId.ValidUserId> = PreConditions(customerCanActCondition())

    override fun getNominalQuery() = NominalQuery<UserId.ValidUserId, Products> { customer ->
        wishlistRepository.findAllBy(customer)
    }

    override fun getPostConditions(): PostConditions<Products> = PostConditions.none()

    private fun customerCanActCondition() = PreCondition<UserId.ValidUserId> { customer ->
        userRepository.verifyCustomerCanAct(customer)
    }
}
