package org.flexstore.domain.actor

import org.flexstore.domain.cart.*
import org.flexstore.domain.cart.usecase.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.*
import org.flexstore.domain.user.usecase.*
import org.flexstore.domain.user.User.DefinedUser
import org.flexstore.domain.user.UserId.ValidUserId
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
        val guest = Guest(addItemToCartUseCase)
        // Act
        guest.performAddItemToCart(AddItemToCartRequest(owner, product.productId, Quantity(1)))
        // Assert
        assertThat(cartRepository.load(owner).findBy(product.productId)).isNotNull
    }
}
