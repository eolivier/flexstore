package org.flexstore.domain.cart.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.UserRepository
import org.flexstore.domain.user.model.AccountRefusedException
import org.flexstore.domain.user.model.UserNotFoundException
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
            val customerId = owner.userId
            if (userRepository.notExists(customerId)) {
                throw UserNotFoundException(ErrorMessage("User ${customerId.value} not found."))
            }
            val status = userRepository.statusOf(customerId)
            if (status.refusesSignIn()) {
                val message = ErrorMessage("The account ${customerId.value} is $status: it cannot act.")
                throw AccountRefusedException(status, message)
            }
        }
    }
}
