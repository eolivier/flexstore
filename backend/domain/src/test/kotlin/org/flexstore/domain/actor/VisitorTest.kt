package org.flexstore.domain.actor

import org.flexstore.domain.cart.*
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

class VisitorTest {

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
        val productRepository = InMemoryProductRepository().apply { save(product) }
        val addItemToCartUseCase = AddItemToCartUseCase(cartRepository, productRepository)
        val visitor = Visitor(addItemToCartUseCase)
        // Act
        visitor.performAddItemToCart(AddItemToCartRequest(owner, product.productId, Quantity(1)))
        // Assert
        assertThat(cartRepository.load(owner).findBy(product.productId)).isNotNull
    }
}
