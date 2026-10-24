package org.flexstore.domain.actor

import org.flexstore.domain.cart.model.*
import org.flexstore.domain.cart.usecase.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.usecase.*
import org.flexstore.domain.user.model.User.DefinedUser
import org.flexstore.domain.user.model.UserId.ValidUserId
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.flexstore.infra.repository.InMemoryUserRepository
import org.flexstore.infra.repository.InMemoryCartRepository
import org.flexstore.infra.repository.InMemoryProductRepository
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import java.math.BigDecimal

class GuestTest {

    @Test
    fun `should perform create user use case`() {
        // Arrange
        val userRepository = spy(InMemoryUserRepository())
        
        val createUserUseCase = CreateUserUseCase(userRepository)
        val guest = Guest(createUserUseCase, mock(AddItemToCartUseCase::class.java))
        val user = DefinedUser(ValidUserId("user1"), Name("John Doe"), Email("1@1.fr"), PlainPassword("password123"))
        // Act
        guest.performCreateUser(user)
        // Assert
        // Verify the user was saved with hashed password by checking repository state
        val savedUser = userRepository.findById(user.id)
        assertThat(savedUser).isInstanceOf(User.DefinedUser::class.java)
        assertThat((savedUser as User.DefinedUser).password).isInstanceOf(HashedPassword::class.java)
        assertThat((savedUser.password as HashedPassword).value).matches("^\\$2[aby]\\$.*")
    }

    @Test
    fun `should perform add item to cart use case`() {
        // Arrange
        val product = DefinedProduct(
            productId = ProductId(Identity("product1")),
            name = Name("product1"),
            price = Price(Amount(BigDecimal(10)), Currency.EUR)
        )
        val owner = CartOwner.Visitor(VisitorCartId.nextId())
        val cartRepository = InMemoryCartRepository()
        val addItemToCartUseCase = AddItemToCartUseCase(cartRepository, InMemoryProductRepository().apply { save(product) })
        val guest = Guest(mock(CreateUserUseCase::class.java), addItemToCartUseCase)
        // Act
        guest.performAddItemToCart(AddItemToCartRequest(owner, product.productId, Quantity(1)))
        // Assert
        assertThat(cartRepository.load(owner).findBy(product.productId)).isNotNull
    }
}
