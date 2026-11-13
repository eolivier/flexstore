package org.flexstore.domain.cart.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.actor.customerId
import org.flexstore.domain.actor.verifyCanAct
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// When a visitor signs in, the cart filled before signing in is merged into the cart of the customer
data class MergeCartsRequest(val visitor: CartOwner.Visitor, val customer: Actor) {

    fun customerCart() = CartOwner.Customer(customer.customerId())
}

class MergeCartsUseCase(private val cartRepository: CartRepository) : UseCase<MergeCartsRequest> {

    override fun getPreConditions(): PreConditions<MergeCartsRequest> =
        PreConditions(customerCanActCondition())

    override fun getNominalScenario(): NominalScenario<MergeCartsRequest> {
        val mergeStep = Step<MergeCartsRequest> { request ->
            val visitorCart = cartRepository.load(request.visitor)
            val customerCart = cartRepository.load(request.customerCart())
            cartRepository.save(customerCart.mergeWith(visitorCart))
            cartRepository.delete(request.visitor)
        }
        return NominalScenario(listOf(mergeStep))
    }

    override fun getPostConditions(): PostConditions<MergeCartsRequest> =
        PostConditions(visitorCartIsDeletedCondition())

    // The customer, resolved before the use case runs, must be a customer or an admin who can act
    private fun customerCanActCondition() = PreCondition<MergeCartsRequest> { request ->
        request.customer.verifyCanAct()
    }

    private fun visitorCartIsDeletedCondition() = PostCondition<MergeCartsRequest> { request ->
        if (cartRepository.load(request.visitor).hasItems()) {
            val message = ErrorMessage("The visitor cart ${request.visitor.cartId} was not merged.")
            throw CartUpdateFailed(message)
        }
    }
}
