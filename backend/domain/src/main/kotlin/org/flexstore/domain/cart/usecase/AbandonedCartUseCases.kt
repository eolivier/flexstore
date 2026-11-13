package org.flexstore.domain.cart.usecase

import org.flexstore.domain.cart.*
import org.ucop.domain.entity.Actor

// The use cases of the abandoned carts: the carts left since the limit are listed, and their customer reminded
class AbandonedCartUseCases(
    private val listAbandonedCartsUseCase: ListAbandonedCartsUseCase,
    private val remindAbandonedCartUseCase: RemindAbandonedCartUseCase
) {
    fun leftSince(reader: Actor, limit: AbandonmentLimit) =
        listAbandonedCartsUseCase.unfold(AbandonedCartsReading(reader, limit))

    fun remind(reminder: Actor, customer: CartOwner.Customer) =
        remindAbandonedCartUseCase.unfold(AbandonedCartReminding(reminder, customer))
}
