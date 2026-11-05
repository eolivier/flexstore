package org.flexstore.domain.cart.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.user.repository.UserRepository
import org.ucop.domain.entity.*

// The cart of a customer or of a visitor, empty when they have none yet
class ReadCartUseCase(
    private val cartRepository: CartRepository,
    private val userRepository: UserRepository
) : QueryUseCase<CartOwner, Cart> {

    override fun getPreConditions(): PreConditions<CartOwner> = PreConditions(ownerCanActCondition())

    override fun getNominalQuery() = NominalQuery<CartOwner, Cart> { owner -> cartRepository.load(owner) }

    override fun getPostConditions(): PostConditions<Cart> = PostConditions.none()

    // A customer owner must be an existing user whose account is not suspended, locked or disabled
    // (read from the users, not from the request); a visitor has none
    private fun ownerCanActCondition() = PreCondition<CartOwner> { owner ->
        if (owner is CartOwner.Customer) {
            userRepository.verifyCustomerCanAct(owner.userId)
        }
    }
}
