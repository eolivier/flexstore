package org.flexstore.domain.usecase.basket

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.entity.Cart
import org.flexstore.domain.valueobject.*
import org.flexstore.infra.repository.InMemoryCartRepository
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ChangeCartItemQuantityUseCaseTest : WithAssertions {

    private val product = DefinedProduct(ProductId(Identity("product-1")), Name("Product1"), Price(Amount(BigDecimal(10)), Currency.EUR))
    private val cartRepository = InMemoryCartRepository()
    private val useCase = ChangeCartItemQuantityUseCase(cartRepository)
    private val itemId = (cartRepository.save(Cart().add(product, Quantity(1))).items.single() as CartItem).itemId

    @Test
    fun `should change the quantity of the item`() {
        // Arrange
        val request = ChangeCartItemQuantityRequest(itemId, Quantity(4))
        // Act
        useCase.unfold(request)
        // Assert
        assertThat(cartRepository.load().findBy(itemId)?.quantity).isEqualTo(Quantity(4))
    }

    @Test
    fun `a quantity of zero should remove the item`() {
        // Arrange
        val request = ChangeCartItemQuantityRequest(itemId, Quantity(0))
        // Act
        useCase.unfold(request)
        // Assert
        assertThat(cartRepository.load().items).isEmpty()
    }

    @Test
    fun `an item already removed should be ignored`() {
        // Arrange
        useCase.unfold(ChangeCartItemQuantityRequest(itemId, Quantity(0)))
        // Act
        useCase.unfold(ChangeCartItemQuantityRequest(itemId, Quantity(5)))
        // Assert
        assertThat(cartRepository.load().items).isEmpty()
    }
}
