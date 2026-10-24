package org.flexstore.domain.usecase.basket

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.entity.InvalidQuantityException
import org.flexstore.domain.usecase.product.ProductNotFoundException
import org.flexstore.domain.valueobject.*
import org.flexstore.infra.repository.InMemoryCartRepository
import org.flexstore.infra.repository.InMemoryProductRepository
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class AddItemToCartUseCaseTest : WithAssertions {

    private val product = DefinedProduct(ProductId(Identity("product-1")), Name("Product1"), Price(Amount(BigDecimal(10)), Currency.EUR))
    private val cartRepository = InMemoryCartRepository()
    private val useCase = AddItemToCartUseCase(cartRepository, InMemoryProductRepository().apply { save(product) })

    @Test
    fun `should add the catalog product to the cart`() {
        // Arrange
        val request = AddItemToCartRequest(product.productId, Quantity(2))
        // Act
        useCase.unfold(request)
        // Assert
        val item = cartRepository.load().findBy(product.productId) as CartItem
        assertThat(item.product).isEqualTo(product)
        assertThat(item.quantity).isEqualTo(Quantity(2))
    }

    @Test
    fun `adding the product again should change the quantity of its item`() {
        // Arrange
        useCase.unfold(AddItemToCartRequest(product.productId, Quantity(1)))
        // Act
        useCase.unfold(AddItemToCartRequest(product.productId, Quantity(3)))
        // Assert
        assertThat(cartRepository.load().items).hasSize(1)
        assertThat((cartRepository.load().findBy(product.productId) as CartItem).quantity).isEqualTo(Quantity(3))
    }

    @Test
    fun `should not add a product missing from the catalog`() {
        // Arrange
        val request = AddItemToCartRequest(ProductId(Identity("unknown")), Quantity(1))
        // Act
        val thrown = catchThrowable { useCase.unfold(request) }
        // Assert
        assertThat(thrown).isInstanceOf(ProductNotFoundException::class.java)
        assertThat(cartRepository.load().items).isEmpty()
    }

    @Test
    fun `should not add a product without a positive quantity`() {
        // Arrange
        val request = AddItemToCartRequest(product.productId, Quantity(0))
        // Act
        val thrown = catchThrowable { useCase.unfold(request) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidQuantityException::class.java)
        assertThat(cartRepository.load().items).isEmpty()
    }
}
