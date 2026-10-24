package org.flexstore.domain.cart.usecase

import org.flexstore.domain.cart.*

class ReadCartUseCase(private val cartRepository: CartRepository) {
    fun getCart(owner: CartOwner) = cartRepository.load(owner)
}
