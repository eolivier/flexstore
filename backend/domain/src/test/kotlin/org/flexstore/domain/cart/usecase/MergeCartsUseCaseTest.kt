package org.flexstore.domain.cart.usecase

import org.flexstore.domain.cart.model.*
import org.flexstore.domain.product.*
import org.flexstore.domain.shared.*
import org.flexstore.domain.user.model.*
import org.assertj.core.api.WithAssertions
import org.flexstore.infra.repository.InMemoryCartRepository
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class MergeCartsUseCaseTest : WithAssertions {

    private val product1 = DefinedProduct(ProductId(Identity("product-1")), Name("Product1"), Price(Amount(BigDecimal(10)), Currency.EUR))
    private val product2 = DefinedProduct(ProductId(Identity("product-2")), Name("Product2"), Price(Amount(BigDecimal(11)), Currency.EUR))
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val visitor = CartOwner.Visitor(VisitorCartId.nextId())
    private val cartRepository = InMemoryCartRepository()
    private val useCase = MergeCartsUseCase(cartRepository)

    @Test
    fun `should merge the visitor cart into the customer cart`() {
        // Arrange
        cartRepository.save(Cart(customer).add(product1, Quantity(1)))
        cartRepository.save(Cart(visitor).add(product1, Quantity(2)).add(product2, Quantity(1)))
        // Act
        useCase.unfold(MergeCartsRequest(visitor, customer))
        // Assert
        val customerCart = cartRepository.load(customer)
        assertThat((customerCart.findBy(product1.productId) as CartItem).quantity).isEqualTo(Quantity(2))
        assertThat((customerCart.findBy(product2.productId) as CartItem).quantity).isEqualTo(Quantity(1))
    }

    @Test
    fun `should delete the visitor cart`() {
        // Arrange
        cartRepository.save(Cart(visitor).add(product1, Quantity(1)))
        // Act
        useCase.unfold(MergeCartsRequest(visitor, customer))
        // Assert
        assertThat(cartRepository.load(visitor).items).isEmpty()
    }

    @Test
    fun `merging an unknown visitor cart should leave the customer cart unchanged`() {
        // Arrange
        val customerCart = cartRepository.save(Cart(customer).add(product1, Quantity(1)))
        // Act
        useCase.unfold(MergeCartsRequest(visitor, customer))
        // Assert
        assertThat(cartRepository.load(customer)).isEqualTo(customerCart)
    }
}
