package org.flexstore.domain.order.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.assertj.core.api.WithAssertions
import org.flexstore.infra.repository.InMemoryCartRepository
import org.flexstore.infra.repository.InMemoryOrderRepository
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class PlaceOrderUseCaseTest : WithAssertions {

    private val lamp = DefinedProduct(ProductId(Identity("lamp")), Name("Desk lamp"), Price(Amount(BigDecimal(35)), Currency.EUR))
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val cartRepository = InMemoryCartRepository()
    private val orderRepository = InMemoryOrderRepository()
    private val userRepository = InMemoryUserRepository()
    private val useCase = PlaceOrderUseCase(cartRepository, orderRepository, userRepository)
    private val request = OrderPlacement(OrderId("order-1"), customer, Instant.parse("2026-10-24T10:00:00Z"), homeAddresses)

    @Test
    fun `should store the order placed from the cart`() {
        // Arrange
        cartRepository.save(Cart(customer).add(lamp, Quantity(2)))
        // Act
        useCase.unfold(request)
        // Assert
        val order = orderRepository.findById(OrderId("order-1"))
        assertThat(order?.lines?.asList()).containsExactly(OrderLine(lamp.productId, Name("Desk lamp"), lamp.price, Quantity(2)))
    }

    @Test
    fun `should empty the cart`() {
        // Arrange
        cartRepository.save(Cart(customer).add(lamp, Quantity(2)))
        // Act
        useCase.unfold(request)
        // Assert
        assertThat(cartRepository.load(customer).isEmpty()).isTrue()
    }

    @Test
    fun `should not place an order from an empty cart`() {
        // Act
        val thrown = catchThrowable { useCase.unfold(request) }
        // Assert
        assertThat(thrown).isInstanceOf(EmptyCartException::class.java)
        assertThat(orderRepository.findById(OrderId("order-1"))).isNull()
    }

    @ParameterizedTest(name = "an account {0}: order placed = {1}")
    @CsvSource("PENDING_VERIFICATION, true", "ACTIVE, true", "SUSPENDED, false", "LOCKED, false", "DISABLED, false")
    fun `only an account that allows ordering should place an order`(status: AccountStatus, placed: Boolean) {
        // Arrange
        userRepository.save(User.DefinedUser(customer.userId, FullName(FirstName("Jane"), LastName("Doe")), Email("jane@example.com"), PlainPassword("password"), status = status))
        cartRepository.save(Cart(customer).add(lamp, Quantity(2)))
        // Act
        val thrown = catchThrowable { useCase.unfold(request) }
        // Assert
        assertThat(orderRepository.orderIsMissing(request.orderId)).isEqualTo(!placed)
        if (!placed) assertThat(thrown).isInstanceOf(OrderRefusedException::class.java)
    }
}
