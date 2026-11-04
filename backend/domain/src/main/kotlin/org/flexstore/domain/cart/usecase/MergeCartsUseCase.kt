package org.flexstore.domain.cart.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.user.UserRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// When a visitor signs in, the cart filled before signing in is merged into the cart of the customer
data class MergeCartsRequest(val visitor: CartOwner.Visitor, val customer: CartOwner.Customer)

class MergeCartsUseCase(
    private val cartRepository: CartRepository,
    private val userRepository: UserRepository
) : UseCase<MergeCartsRequest> {

    override fun getPreConditions(): PreConditions<MergeCartsRequest> =
        PreConditions(customerCanActCondition())

    override fun getNominalScenario(): NominalScenario<MergeCartsRequest> {
        val mergeStep = Step<MergeCartsRequest> { request ->
            val visitorCart = cartRepository.load(request.visitor)
            val customerCart = cartRepository.load(request.customer)
            cartRepository.save(customerCart.mergeWith(visitorCart))
            cartRepository.delete(request.visitor)
        }
        return NominalScenario(listOf(mergeStep))
    }

    override fun getPostConditions(): PostConditions<MergeCartsRequest> =
        PostConditions(visitorCartIsDeletedCondition())

    // The customer must be an existing user whose account is not suspended, locked or disabled (read
    // from the users, not from the request)
    private fun customerCanActCondition() = PreCondition<MergeCartsRequest> { request ->
        userRepository.verifyCustomerCanAct(request.customer.userId)
    }

    private fun visitorCartIsDeletedCondition() = PostCondition<MergeCartsRequest> { request ->
        if (cartRepository.load(request.visitor).hasItems()) {
            val message = ErrorMessage("The visitor cart ${request.visitor.cartId.value} was not merged.")
            throw CartUpdateFailed(message)
        }
    }
}
