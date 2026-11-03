package org.flexstore.domain.cart.usecase

import org.flexstore.domain.user.blockedCustomerId
import org.flexstore.domain.user.customerWithStatus
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.customersWithIds
import org.flexstore.domain.user.unknownCustomerId
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
    private val userRepository = customersWithIds("user-1")
    private val useCase = MergeCartsUseCase(cartRepository, userRepository)

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
        assertThat(cartRepository.load(visitor).items.asList()).isEmpty()
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

    @Test
    fun `an unknown customer should not merge a visitor cart`() {
        // Arrange
        cartRepository.save(Cart(visitor).add(product1, Quantity(1)))
        val unknownCustomer = CartOwner.Customer(unknownCustomerId)
        // Act
        val thrown = catchThrowable { useCase.unfold(MergeCartsRequest(visitor, unknownCustomer)) }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException::class.java)
        assertThat(cartRepository.load(visitor).hasItems()).isTrue()
    }

    @ParameterizedTest
    @EnumSource(AccountStatus::class, names = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `a customer whose account is refused should not merge a visitor cart`(status: AccountStatus) {
        // Arrange
        userRepository.save(customerWithStatus(status))
        val blockedCustomer = CartOwner.Customer(blockedCustomerId)
        // Act
        val thrown = catchThrowable { useCase.unfold(MergeCartsRequest(visitor, blockedCustomer)) }
        // Assert
        assertThat(thrown).isInstanceOf(AccountRefusedException::class.java)
    }
}
