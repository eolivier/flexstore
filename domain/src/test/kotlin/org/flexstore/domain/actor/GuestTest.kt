package org.flexstore.domain.actor

import org.junit.jupiter.api.Assertions.*

import org.flexstore.domain.*
import org.flexstore.domain.entity.Basket
import org.flexstore.domain.entity.Email
import org.flexstore.domain.entity.User
import org.flexstore.domain.entity.UserId
import org.flexstore.domain.repository.UserRepository
import org.flexstore.domain.usecase.CreateUserUseCase
import org.flexstore.domain.usecase.AddItemToBasketUseCase
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.math.BigDecimal

class GuestTest {

    @Test
    fun `should perform create user use case`() {
        val userRepository = mock(UserRepository::class.java)
        val createUserUseCase = CreateUserUseCase(userRepository)
        val guest = Guest(createUserUseCase, mock(AddItemToBasketUseCase::class.java))
        val user = User(UserId("user1"), Name("John Doe"), Email("1@1.fr"))

        guest.performCreateUser(user)

        verify(userRepository).save(user)
    }

    @Test
    fun `should perform add item to basket use case`() {
        //val basketRepository = mock(BasketRepository::class.java)
        val basket = Basket()
        val addItemToBasketUseCase = AddItemToBasketUseCase(basket)
        val guest = Guest(mock(CreateUserUseCase::class.java), addItemToBasketUseCase)
        //val basketId = BasketId(Identity("basket1"))
        val item = Item(ItemId(Identity("item1")), Product(ProductId(Identity("product1")), Name("product1"), Price(Amount(
            BigDecimal(10)
        ), Currency.EUR)), Quantity(1))

        guest.performAddItemToBasket(item, Quantity(1))

        //verify(basketRepository).addItem(basketId, item)
    }
}