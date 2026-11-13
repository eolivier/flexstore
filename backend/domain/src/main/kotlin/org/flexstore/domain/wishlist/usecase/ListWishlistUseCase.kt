package org.flexstore.domain.wishlist.usecase

import org.flexstore.domain.product.model.Products
import org.flexstore.domain.actor.customerId
import org.flexstore.domain.actor.verifyCanAct
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.wishlist.WishlistRepository
import org.ucop.domain.entity.*

// The products a customer saved to their wishlist, newest first, to render them like the catalog
class ListWishlistUseCase(private val wishlistRepository: WishlistRepository) : QueryUseCase<Actor, Products> {

    override fun getPreConditions(): PreConditions<Actor> = PreConditions(customerCanActCondition())

    override fun getNominalQuery() = NominalQuery<Actor, Products> { customer ->
        wishlistRepository.findAllBy(customer.customerId())
    }

    override fun getPostConditions(): PostConditions<Products> = PostConditions.none()

    // The customer, resolved before the use case runs, must be a customer or an admin who can act
    private fun customerCanActCondition() = PreCondition<Actor> { customer -> customer.verifyCanAct() }
}
