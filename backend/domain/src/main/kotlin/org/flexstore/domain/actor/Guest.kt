package org.flexstore.domain.actor

import org.flexstore.domain.cart.usecase.*

// A visitor of the shop: they fill their cart (creating users is reserved to the administrators)
class Guest(private val addItemToCartUseCase: AddItemToCartUseCase) {

    fun performAddItemToCart(request: AddItemToCartRequest) {
        addItemToCartUseCase.unfold(request)
    }
}
