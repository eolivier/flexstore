package org.flexstore.domain.cart.usecase

import org.ucop.domain.UseCaseLog
import org.flexstore.domain.cart.*

class ReadCartUseCase(private val cartRepository: CartRepository) {
    fun getCart(owner: CartOwner) =
        UseCaseLog.execution("ReadCartUseCase.getCart") { cartRepository.load(owner) }
}
