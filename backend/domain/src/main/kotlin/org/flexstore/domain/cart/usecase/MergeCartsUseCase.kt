package org.flexstore.domain.cart.usecase

import org.flexstore.domain.cart.*
import org.ucop.domain.NonEmptyString
import org.ucop.domain.entity.*

// When a visitor signs in, the cart filled before signing in is merged into the cart of the customer
data class MergeCartsRequest(val visitor: CartOwner.Visitor, val customer: CartOwner.Customer)

class MergeCartsUseCase(private val cartRepository: CartRepository) : UseCase<MergeCartsRequest> {

    override fun getPreConditions(): List<PreCondition<MergeCartsRequest>> = listOf(EmptyPreCondition())

    override fun getNominalScenario(): NominalScenario<MergeCartsRequest> {
        val mergeStep = Step<MergeCartsRequest> { request ->
            val visitorCart = cartRepository.load(request.visitor)
            val customerCart = cartRepository.load(request.customer)
            cartRepository.save(customerCart.mergeWith(visitorCart))
            cartRepository.delete(request.visitor)
        }
        return NominalScenario(listOf(mergeStep))
    }

    override fun getPostConditions(): List<PostCondition<MergeCartsRequest>> = listOf(visitorCartIsDeletedCondition())

    private fun visitorCartIsDeletedCondition() = PostCondition<MergeCartsRequest> { request ->
        if (cartRepository.load(request.visitor).hasItems()) {
            throw CartUpdateFailed(NonEmptyString("The visitor cart ${request.visitor.cartId.value} was not merged."))
        }
    }
}
