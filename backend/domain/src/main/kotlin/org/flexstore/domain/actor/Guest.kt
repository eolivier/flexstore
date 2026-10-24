package org.flexstore.domain.actor

import org.flexstore.domain.cart.usecase.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.usecase.*

class Guest(
    private val createUserUseCase: CreateUserUseCase,
    private val addItemToCartUseCase: AddItemToCartUseCase
) {

    fun performCreateUser(user: User) {
        createUserUseCase.unfold(user)
    }

    fun performAddItemToCart(request: AddItemToCartRequest) {
        addItemToCartUseCase.unfold(request)
    }
}
