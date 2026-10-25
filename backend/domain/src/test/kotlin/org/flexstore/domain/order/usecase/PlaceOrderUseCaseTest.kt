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

    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), Price(Amount(BigDecimal(35)), Currency.EUR))
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val jane = User.DefinedUser(customer.userId, FullName(FirstName("Jane"), LastName("Doe")), Email("jane@example.com"), PlainPassword("password"))
    private val office = PostalAddress(Street("3 place du Marché"), null, PostalCode("69001"), City("Lyon"), Country("France"))
    private val cartRepository = InMemoryCartRepository()
    private val orderRepository = InMemoryOrderRepository()
    private val userRepository = InMemoryUserRepository().apply { save(jane) }
    private val useCase = PlaceOrderUseCase(cartRepository, orderRepository, userRepository)
    private val placedAt = Instant.parse("2026-10-24T10:00:00Z")
    private val orderPlacement = OrderPlacement(OrderId("order-1"), customer, placedAt, homeAddresses)

    private fun addressesOfJane() = (userRepository.findById(customer.userId) as User.DefinedUser).postalAddresses.asList()

    @Test
    fun `should store the order placed from the cart`() {
        // Arrange
        cartRepository.save(Cart(customer).add(deskLamp, Quantity(2)))
        // Act
        useCase.unfold(orderPlacement)
        // Assert
        val order = orderRepository.findById(OrderId("order-1"))
        assertThat(order?.lines?.asList()).containsExactly(OrderLine(deskLamp.productId, Name("Desk lamp"), deskLamp.price, Quantity(2)))
    }

    @Test
    fun `should empty the cart`() {
        // Arrange
        cartRepository.save(Cart(customer).add(deskLamp, Quantity(2)))
        // Act
        useCase.unfold(orderPlacement)
        // Assert
        assertThat(cartRepository.load(customer).isEmpty()).isTrue()
    }

    @Test
    fun `should not place an order from an empty cart`() {
        // Act
        val thrown = catchThrowable { useCase.unfold(orderPlacement) }
        // Assert
        assertThat(thrown).isInstanceOf(EmptyCartException::class.java)
        assertThat(orderRepository.findById(OrderId("order-1"))).isNull()
    }

    @Test
    fun `an unknown customer should not place an order`() {
        // Arrange
        val stranger = CartOwner.Customer(UserId.ValidUserId("stranger"))
        cartRepository.save(Cart(stranger).add(deskLamp, Quantity(2)))
        val strangerOrderPlacement = OrderPlacement(OrderId("order-1"), stranger, placedAt, homeAddresses)
        // Act
        val thrown = catchThrowable { useCase.unfold(strangerOrderPlacement) }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `the addresses entered when ordering should become addresses of the customer`() {
        // Arrange
        cartRepository.save(Cart(customer).add(deskLamp, Quantity(2)))
        val deliveredHomeBilledToTheOffice = orderPlacement.copy(addresses = OrderAddresses(home, office))
        // Act
        useCase.unfold(deliveredHomeBilledToTheOffice)
        // Assert
        assertThat(addressesOfJane()).containsExactly(home, office)
    }

    @Test
    fun `an address the customer already has should not be added twice`() {
        // Arrange
        userRepository.save(jane.copy(postalAddresses = PostalAddresses(home)))
        cartRepository.save(Cart(customer).add(deskLamp, Quantity(2)))
        // Act
        useCase.unfold(orderPlacement)
        // Assert
        assertThat(addressesOfJane()).containsExactly(home)
    }

    @ParameterizedTest(name = "an account {0}: order placed = {1}")
    @CsvSource("PENDING_VERIFICATION, true", "ACTIVE, true", "SUSPENDED, false", "LOCKED, false", "DISABLED, false")
    fun `only an account that allows ordering should place an order`(status: AccountStatus, placed: Boolean) {
        // Arrange
        userRepository.save(jane.copy(status = status))
        cartRepository.save(Cart(customer).add(deskLamp, Quantity(2)))
        // Act
        val thrown = catchThrowable { useCase.unfold(orderPlacement) }
        // Assert
        assertThat(orderRepository.orderIsMissing(orderPlacement.orderId)).isEqualTo(!placed)
        if (!placed) assertThat(thrown).isInstanceOf(OrderRefusedException::class.java)
    }
}
