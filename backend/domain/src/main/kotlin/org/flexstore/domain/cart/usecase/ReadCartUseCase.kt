package org.flexstore.domain.cart.usecase

import org.flexstore.domain.cart.*
import org.ucop.domain.entity.*

// The cart of a customer or of a visitor, empty when they have none yet
class ReadCartUseCase(private val cartRepository: CartRepository) : QueryUseCase<CartOwner, Cart> {

    override fun getPreConditions(): PreConditions<CartOwner> = PreConditions.none()

    override fun getNominalQuery() = NominalQuery<CartOwner, Cart> { owner -> cartRepository.load(owner) }

    override fun getPostConditions(): PostConditions<Cart> = PostConditions.none()
}
