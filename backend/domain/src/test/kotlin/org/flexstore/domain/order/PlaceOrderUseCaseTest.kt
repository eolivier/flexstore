package org.flexstore.domain.order

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.entity.*
import org.flexstore.domain.valueobject.*
import org.flexstore.infra.repository.InMemoryCartRepository
import org.flexstore.infra.repository.InMemoryOrderRepository
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class PlaceOrderUseCaseTest : WithAssertions {

    private val lamp = DefinedProduct(ProductId(Identity("lamp")), Name("Desk lamp"), Price(Amount(BigDecimal(35)), Currency.EUR))
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val cartRepository = InMemoryCartRepository()
    private val orderRepository = InMemoryOrderRepository()
    private val useCase = PlaceOrderUseCase(cartRepository, orderRepository)
    private val request = PlaceOrderRequest(OrderId("order-1"), customer, Instant.parse("2026-10-24T10:00:00Z"))

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
}
