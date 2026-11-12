package org.flexstore.domain.cart.usecase

import org.flexstore.domain.actor.cannotRunScheduledUseCases
import org.flexstore.domain.cart.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// The carts of the customers left unchanged since the limit, which no reminder was sent for
class ListAbandonedCartsUseCase(
    private val cartRepository: CartRepository
) : QueryUseCase<AbandonedCartsReading, AbandonedCarts> {

    override fun getPreConditions(): PreConditions<AbandonedCartsReading> =
        PreConditions(readerCanRunScheduledUseCasesCondition())

    override fun getNominalQuery() = NominalQuery<AbandonedCartsReading, AbandonedCarts> { reading ->
        cartRepository.abandonedCartsAt(reading.limit)
    }

    override fun getPostConditions(): PostConditions<AbandonedCarts> = PostConditions.none()

    // Only an Admin or the system reads the carts that a scheduler reminds
    private fun readerCanRunScheduledUseCasesCondition() = PreCondition<AbandonedCartsReading> { reading ->
        if (reading.reader.cannotRunScheduledUseCases()) {
            throw AbandonedCartForbiddenException(
                ErrorMessage("Only an administrator or the system can read the abandoned carts.")
            )
        }
    }
}
