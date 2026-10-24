package org.flexstore.domain.actor

import org.flexstore.domain.entity.User
import org.flexstore.domain.usecase.basket.AddItemToCartRequest
import org.flexstore.domain.usecase.basket.AddItemToCartUseCase
import org.flexstore.domain.usecase.user.CreateUserUseCase

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
