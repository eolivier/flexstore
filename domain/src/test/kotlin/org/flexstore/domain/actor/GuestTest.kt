package org.flexstore.domain.actor

import org.flexstore.domain.*
import org.flexstore.domain.entity.Basket
import org.flexstore.domain.entity.Email
import org.flexstore.domain.entity.User.DefinedUser
import org.flexstore.domain.entity.UserId.ValidUserId
import org.flexstore.domain.repository.UserRepository
import org.flexstore.domain.usecase.basket.AddItemToBasketUseCase
import org.flexstore.domain.usecase.user.CreateUserUseCase
import org.flexstore.domain.valueobject.*
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.ucop.domain.entity.Name
import java.math.BigDecimal

class GuestTest {

    @Test
    fun `should perform create user use case`() {
        // Arrange
        val userRepository = mock(UserRepository::class.java)
        val createUserUseCase = CreateUserUseCase(userRepository)
        val guest = Guest(createUserUseCase, mock(AddItemToBasketUseCase::class.java))
        val user = DefinedUser(ValidUserId("user1"), Name("John Doe"), Email("1@1.fr"))
        // Act
        guest.performCreateUser(user)
        // Assert
        verify(userRepository).save(user)
    }

    @Test
    fun `should perform add item to basket use case`() {
        // Arrange
        val basket = Basket()
        val addItemToBasketUseCase = AddItemToBasketUseCase(basket)
        val guest = Guest(mock(CreateUserUseCase::class.java), addItemToBasketUseCase)
        val item = Item(
            ItemId(Identity("item1")), Product(
                ProductId(Identity("product1")), Name("product1"), Price(
                    Amount(
            BigDecimal(10)
        ), Currency.EUR)
            ), Quantity(1)
        )
        // Act
        guest.performAddItemToBasket(item, Quantity(1))
        // Assert
        //verify(basketRepository).addItem(basketId, item)
    }
}