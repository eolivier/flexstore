package org.flexstore.domain.cart.usecase

import org.flexstore.domain.cart.*
import org.ucop.domain.entity.*

// The cart of a customer or of a visitor, empty when they have none yet
data class CartReading(val reader: Actor, val owner: CartOwner)

class ReadCartUseCase(private val cartRepository: CartRepository) : QueryUseCase<CartReading, Cart> {

    override fun getPreConditions(): PreConditions<CartReading> = PreConditions(ownerCanActCondition())

    override fun getNominalQuery() = NominalQuery<CartReading, Cart> { reading ->
        cartRepository.load(reading.owner)
    }

    override fun getPostConditions(): PostConditions<Cart> = PostConditions.none()

    // The reader, resolved before the use case runs, must be able to act when the cart is the one of a customer
    private fun ownerCanActCondition() = PreCondition<CartReading> { reading ->
        reading.reader.verifyCanUseCartOf(reading.owner)
    }
}
