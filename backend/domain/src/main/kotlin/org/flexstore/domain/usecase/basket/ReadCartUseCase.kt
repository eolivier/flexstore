package org.flexstore.domain.usecase.basket

import org.flexstore.domain.entity.CartOwner
import org.flexstore.domain.repository.CartRepository

class ReadCartUseCase(private val cartRepository: CartRepository) {
    fun getCart(owner: CartOwner) = cartRepository.load(owner)
}
