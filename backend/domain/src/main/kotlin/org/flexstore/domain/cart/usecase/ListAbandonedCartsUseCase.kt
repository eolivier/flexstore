package org.flexstore.domain.cart.usecase

import org.flexstore.domain.cart.*
import org.ucop.domain.entity.*

// The carts of the customers left unchanged since the limit, which no reminder was sent for
class ListAbandonedCartsUseCase(
    private val cartRepository: CartRepository
) : QueryUseCase<AbandonmentLimit, AbandonedCarts> {

    override fun getPreConditions(): PreConditions<AbandonmentLimit> = PreConditions.none()

    override fun getNominalQuery() = NominalQuery<AbandonmentLimit, AbandonedCarts> { limit ->
        cartRepository.abandonedCartsAt(limit)
    }

    override fun getPostConditions(): PostConditions<AbandonedCarts> = PostConditions.none()
}
