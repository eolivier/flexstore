package org.flexstore.domain.actor

import org.flexstore.domain.valueobject.Item
import org.flexstore.domain.valueobject.Quantity
import org.flexstore.domain.entity.User
import org.flexstore.domain.usecase.basket.AddItemToBasketUseCase
import org.flexstore.domain.usecase.user.CreateUserUseCase

class Guest(
    private val createUserUseCase: CreateUserUseCase,
    private val addItemToBasketUseCase: AddItemToBasketUseCase
) {

    fun performCreateUser(user: User) {
        createUserUseCase.run(user)
    }

    fun performAddItemToBasket(newItem: Item, quantity: Quantity) {
        addItemToBasketUseCase.perform(newItem, quantity)
    }
}