package org.flexstore.domain.actor

import org.flexstore.domain.entity.Cart
import org.flexstore.domain.entity.Email
import org.flexstore.domain.entity.User.DefinedUser
import org.flexstore.domain.entity.UserId.ValidUserId
import org.flexstore.domain.usecase.basket.AddItemToBasketUseCase
import org.flexstore.domain.usecase.user.CreateUserUseCase
import org.flexstore.domain.valueobject.*
import org.flexstore.infra.repository.InMemoryItemRepository
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.math.BigDecimal

class GuestTest {

    @Test
    fun `should perform create user use case`() {
        // Arrange
        val userRepository = spy(InMemoryUserRepository())
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
        val itemRepository = spy(InMemoryItemRepository())
        itemRepository.clear()
        val cart = Cart(itemRepository)
        val addItemToBasketUseCase = AddItemToBasketUseCase(cart)
        val guest = Guest(mock(CreateUserUseCase::class.java), addItemToBasketUseCase)
        val draftItem = DraftItem(
            product = Product(
                productId = ProductId(Identity("product1")),
                name = Name("product1"),
                price = Price(Amount(BigDecimal(10)), Currency.EUR)
            ),
            quantity = Quantity(1)
        )
        // Act
        guest.performAddItemToBasket(draftItem, Quantity(1))
        // Assert
        verify(itemRepository).add(draftItem)
    }
}