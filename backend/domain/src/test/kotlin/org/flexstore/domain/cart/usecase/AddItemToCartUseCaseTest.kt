package org.flexstore.domain.cart.usecase

import org.flexstore.domain.cart.model.*
import org.flexstore.domain.product.*
import org.flexstore.domain.shared.*
import org.flexstore.domain.user.model.*
import org.assertj.core.api.WithAssertions
import org.flexstore.infra.repository.InMemoryCartRepository
import org.flexstore.infra.repository.InMemoryProductRepository
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class AddItemToCartUseCaseTest : WithAssertions {

    private val product = DefinedProduct(ProductId(Identity("product-1")), Name("Product1"), Price(Amount(BigDecimal(10)), Currency.EUR))
    private val owner = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val cartRepository = InMemoryCartRepository()
    private val useCase = AddItemToCartUseCase(cartRepository, InMemoryProductRepository().apply { save(product) })

    @Test
    fun `should add the catalog product to the cart`() {
        // Arrange
        val request = AddItemToCartRequest(owner, product.productId, Quantity(2))
        // Act
        useCase.unfold(request)
        // Assert
        val item = cartRepository.load(owner).findBy(product.productId) as CartItem
        assertThat(item.product).isEqualTo(product)
        assertThat(item.quantity).isEqualTo(Quantity(2))
    }

    @Test
    fun `adding the product again should change the quantity of its item`() {
        // Arrange
        useCase.unfold(AddItemToCartRequest(owner, product.productId, Quantity(1)))
        // Act
        useCase.unfold(AddItemToCartRequest(owner, product.productId, Quantity(3)))
        // Assert
        assertThat(cartRepository.load(owner).items.asList()).hasSize(1)
        assertThat((cartRepository.load(owner).findBy(product.productId) as CartItem).quantity).isEqualTo(Quantity(3))
    }

    @Test
    fun `should not add a product missing from the catalog`() {
        // Arrange
        val request = AddItemToCartRequest(owner, ProductId(Identity("unknown")), Quantity(1))
        // Act
        val thrown = catchThrowable { useCase.unfold(request) }
        // Assert
        assertThat(thrown).isInstanceOf(ProductNotFoundException::class.java)
        assertThat(cartRepository.load(owner).items.asList()).isEmpty()
    }

    @Test
    fun `should not add a product without a positive quantity`() {
        // Arrange
        val request = AddItemToCartRequest(owner, product.productId, Quantity(0))
        // Act
        val thrown = catchThrowable { useCase.unfold(request) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidQuantityException::class.java)
        assertThat(cartRepository.load(owner).items.asList()).isEmpty()
    }

    @Test
    fun `should add the item to the cart of its owner only`() {
        // Arrange
        val otherOwner = CartOwner.Customer(UserId.ValidUserId("user-2"))
        // Act
        useCase.unfold(AddItemToCartRequest(owner, product.productId, Quantity(1)))
        // Assert
        assertThat(cartRepository.load(otherOwner).items.asList()).isEmpty()
    }
}
