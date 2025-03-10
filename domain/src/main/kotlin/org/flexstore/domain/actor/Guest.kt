package org.flexstore.domain.actor

import org.flexstore.domain.Item
import org.flexstore.domain.Quantity
import org.flexstore.domain.entity.User
import org.flexstore.domain.usecase.CreateUserUseCase
import org.flexstore.domain.usecase.AddItemToBasketUseCase

class Guest(
    private val createUserUseCase: CreateUserUseCase,
    private val addItemToBasketUseCase: AddItemToBasketUseCase
) {

    fun performCreateUser(user: User) {
        createUserUseCase.perform(user)
    }

    fun performAddItemToBasket(newItem: Item, quantity: Quantity) {
        addItemToBasketUseCase.perform(newItem, quantity)
    }
}