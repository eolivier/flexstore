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
    private val definedUser = User.DefinedUser(customer.userId, FullName(FirstName("Jane"), LastName("Doe")), Email("jane@example.com"), PlainPassword("password"))
    private val cartRepository = InMemoryCartRepository()
    private val orderRepository = InMemoryOrderRepository()
    private val userRepository = InMemoryUserRepository().apply { save(definedUser) }
    private val useCase = PlaceOrderUseCase(cartRepository, orderRepository, userRepository)
    private val placedAt = Instant.parse("2026-10-24T10:00:00Z")
    private val orderStamp = OrderStamp(OrderId("order-1"), placedAt)
    private val orderPlacement = OrderPlacement(orderStamp, Checkout(customer, homeOrderAddresses))

    private fun postalAddressesOfTheDefinedUser() = (userRepository.findById(customer.userId) as User.DefinedUser).postalAddresses.asList()

    @Test
    fun `should store the order placed from the cart`() {
        // Arrange
        cartRepository.save(Cart(customer).add(deskLamp, Quantity(2)))
        // Act
        useCase.unfold(orderPlacement)
        // Assert
        val order = orderRepository.findById(OrderId("order-1"))
        assertThat(order?.lines?.asList()).containsExactly(OrderLine.of(deskLamp, Quantity(2)))
    }

    @Test
    fun `should keep the cart until the order is paid`() {
        // Arrange
        cartRepository.save(Cart(customer).add(deskLamp, Quantity(2)))
        // Act
        useCase.unfold(orderPlacement)
        // Assert
        assertThat(cartRepository.load(customer).hasItems()).isTrue()
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
        val unknownCustomer = CartOwner.Customer(UserId.ValidUserId("unknown-customer"))
        cartRepository.save(Cart(unknownCustomer).add(deskLamp, Quantity(2)))
        val unknownCustomerOrderPlacement = OrderPlacement(orderStamp, Checkout(unknownCustomer, homeOrderAddresses))
        // Act
        val thrown = catchThrowable { useCase.unfold(unknownCustomerOrderPlacement) }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `the addresses entered when ordering should become addresses of the customer`() {
        // Arrange
        cartRepository.save(Cart(customer).add(deskLamp, Quantity(2)))
        val orderPlacementBilledToTheOffice = orderPlacement.copy(checkout = Checkout(customer, OrderAddresses(homeAddress, officeAddress)))
        // Act
        useCase.unfold(orderPlacementBilledToTheOffice)
        // Assert
        assertThat(postalAddressesOfTheDefinedUser()).containsExactly(homeAddress, officeAddress)
    }

    @Test
    fun `an address the customer already has should not be added twice`() {
        // Arrange
        userRepository.save(definedUser.copy(postalAddresses = PostalAddresses(homeAddress)))
        cartRepository.save(Cart(customer).add(deskLamp, Quantity(2)))
        // Act
        useCase.unfold(orderPlacement)
        // Assert
        assertThat(postalAddressesOfTheDefinedUser()).containsExactly(homeAddress)
    }

    @ParameterizedTest(name = "an account {0}: order placed = {1}")
    @CsvSource("PENDING_VERIFICATION, true", "ACTIVE, true", "SUSPENDED, false", "LOCKED, false", "DISABLED, false")
    fun `only an account that allows ordering should place an order`(status: AccountStatus, placed: Boolean) {
        // Arrange
        userRepository.save(definedUser.copy(status = status))
        cartRepository.save(Cart(customer).add(deskLamp, Quantity(2)))
        // Act
        val thrown = catchThrowable { useCase.unfold(orderPlacement) }
        // Assert
        assertThat(orderRepository.orderIsMissing(orderPlacement.orderId)).isEqualTo(!placed)
        if (!placed) assertThat(thrown).isInstanceOf(OrderRefusedException::class.java)
    }
}
