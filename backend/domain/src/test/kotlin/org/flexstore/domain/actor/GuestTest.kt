package org.flexstore.domain.actor

import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.flexstore.domain.entity.*
import org.flexstore.domain.entity.User.DefinedUser
import org.flexstore.domain.entity.UserId.ValidUserId
import org.flexstore.domain.usecase.basket.AddItemToBasketUseCase
import org.flexstore.domain.usecase.user.CreateUserUseCase
import org.flexstore.domain.valueobject.*
import org.flexstore.infra.repository.InMemoryUserRepository
import org.flexstore.infra.repository.InMemoryItemRepository
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
        val user = DefinedUser(ValidUserId("user1"), Name("John Doe"), Email("1@1.fr"), Password("password123"))
        // Act
        guest.performCreateUser(user)
        // Assert
        // Verify the user was saved with hashed password by checking repository state
        val savedUser = userRepository.findById(user.id)
        assertThat(savedUser).isInstanceOf(User.DefinedUser::class.java)
        assertThat((savedUser as User.DefinedUser).password.value).matches("^\\$2[aby]\\$.*")
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
            product = DefinedProduct(
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