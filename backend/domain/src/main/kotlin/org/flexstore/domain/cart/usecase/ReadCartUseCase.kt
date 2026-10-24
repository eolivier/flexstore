package org.flexstore.domain.cart.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.cart.model.*

class ReadCartUseCase(private val cartRepository: CartRepository) {
    fun getCart(owner: CartOwner) = cartRepository.load(owner)
}
