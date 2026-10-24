package org.flexstore.domain.usecase.basket

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.entity.Cart
import org.flexstore.domain.entity.CartOwner
import org.flexstore.domain.entity.UserId
import org.flexstore.domain.valueobject.*
import org.flexstore.infra.repository.InMemoryCartRepository
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class ChangeCartItemQuantityUseCaseTest : WithAssertions {

    private val product = DefinedProduct(ProductId(Identity("product-1")), Name("Product1"), Price(Amount(BigDecimal(10)), Currency.EUR))
    private val owner = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val cartRepository = InMemoryCartRepository()
    private val useCase = ChangeCartItemQuantityUseCase(cartRepository)
    private val itemId = (cartRepository.save(Cart(owner).add(product, Quantity(1))).items.single() as CartItem).itemId

    @Test
    fun `should change the quantity of the item`() {
        // Arrange
        val request = ChangeCartItemQuantityRequest(owner, itemId, Quantity(4))
        // Act
        useCase.unfold(request)
        // Assert
        assertThat(cartRepository.load(owner).findBy(itemId)?.quantity).isEqualTo(Quantity(4))
    }

    @Test
    fun `a quantity of zero should remove the item`() {
        // Arrange
        val request = ChangeCartItemQuantityRequest(owner, itemId, Quantity(0))
        // Act
        useCase.unfold(request)
        // Assert
        assertThat(cartRepository.load(owner).items).isEmpty()
    }

    @Test
    fun `an item already removed should be ignored`() {
        // Arrange
        useCase.unfold(ChangeCartItemQuantityRequest(owner, itemId, Quantity(0)))
        // Act
        useCase.unfold(ChangeCartItemQuantityRequest(owner, itemId, Quantity(5)))
        // Assert
        assertThat(cartRepository.load(owner).items).isEmpty()
    }

    @Test
    fun `should not change an item of another cart`() {
        // Arrange
        val otherOwner = CartOwner.Customer(UserId.ValidUserId("user-2"))
        // Act
        useCase.unfold(ChangeCartItemQuantityRequest(otherOwner, itemId, Quantity(0)))
        // Assert
        assertThat(cartRepository.load(owner).findBy(itemId)?.quantity).isEqualTo(Quantity(1))
    }
}
