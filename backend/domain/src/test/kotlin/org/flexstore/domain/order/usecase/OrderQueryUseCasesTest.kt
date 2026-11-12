package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.model.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.user.blockedCustomerId
import org.flexstore.domain.user.customerWithStatus
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.flexstore.domain.user.customersWithIds
import org.flexstore.domain.user.unknownCustomerId
import org.assertj.core.api.WithAssertions
import org.flexstore.infra.repository.InMemoryOrderRepository
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class OrderQueryUseCasesTest : WithAssertions {

    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), Price(Amount(BigDecimal(35)), Currency.EUR))
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val otherCustomer = CartOwner.Customer(UserId.ValidUserId("user-2"))
    private val placedAt = Instant.parse("2026-10-24T10:00:00Z")
    private val orderRepository = InMemoryOrderRepository()
    private val userRepository = customersWithIds("user-1", "user-2")
    private val listCustomerOrdersUseCase = ListCustomerOrdersUseCase(orderRepository, userRepository)
    private val readCustomerOrderUseCase = ReadCustomerOrderUseCase(orderRepository, userRepository)
    private val readAnyOrderUseCase = ReadAnyOrderUseCase(orderRepository)
    private val listAllOrdersUseCase = ListAllOrdersUseCase(orderRepository)

    private fun placeOrder(orderId: OrderId, customer: CartOwner.Customer, placedAt: Instant): Order {
        val orderStamp = OrderStamp(orderId, placedAt)
        val checkout = Checkout(customer, homeOrderAddresses, acceptedTermsOfSale)
        val orderPlacement = OrderPlacement(orderStamp, checkout)
        val cart = Cart(customer).add(deskLamp, Quantity(1))
        val order = Order.place(orderPlacement, cart)
        return orderRepository.save(order)
    }

    @Test
    fun `should list the orders of the customer only, newest first`() {
        // Arrange
        val older = placeOrder(OrderId("older"), customer, placedAt)
        val newer = placeOrder(OrderId("newer"), customer, placedAt.plusSeconds(60))
        placeOrder(OrderId("other"), otherCustomer, placedAt)
        // Act
        val page = listCustomerOrdersUseCase.unfold(CustomerOrdersReading(customer.userId, PageRequest.first()))
        // Assert
        assertThat(page.content.asList()).containsExactly(newer, older)
        assertThat(page.total).isEqualTo(TotalCount(2))
    }

    @Test
    fun `should read an order of the customer`() {
        // Arrange
        val order = placeOrder(OrderId("order-1"), customer, placedAt)
        // Act
        val readOrder = readCustomerOrderUseCase.unfold(CustomerOrderLookup(customer.userId, OrderId("order-1")))
        // Assert
        assertThat(readOrder).isEqualTo(order)
    }

    @Test
    fun `the order of another customer should not be found`() {
        // Arrange
        placeOrder(OrderId("other"), otherCustomer, placedAt)
        // Act
        val thrown = catchThrowable { readCustomerOrderUseCase.unfold(CustomerOrderLookup(customer.userId, OrderId("other"))) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(OrderNotFoundException::class.java)
    }

    @Test
    fun `an unknown order should not be found`() {
        // Act
        val thrown = catchThrowable { readCustomerOrderUseCase.unfold(CustomerOrderLookup(customer.userId, OrderId("unknown"))) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(OrderNotFoundException::class.java)
    }

    @Test
    fun `any order should be read, whoever its customer`() {
        // Arrange
        val order = placeOrder(OrderId("other"), otherCustomer, placedAt)
        // Act
        val readOrder = readAnyOrderUseCase.unfold(OrderId("other"))
        // Assert
        assertThat(readOrder).isEqualTo(order)
    }

    @Test
    fun `the orders of all the customers should be listed, newest first`() {
        // Arrange
        val older = placeOrder(OrderId("older"), customer, placedAt)
        val newer = placeOrder(OrderId("newer"), otherCustomer, placedAt.plusSeconds(60))
        // Act
        val orders = listAllOrdersUseCase.unfold(Unit)
        // Assert
        assertThat(orders.asList()).containsExactly(newer, older)
    }

    @Test
    fun `an unknown customer should not list orders`() {
        // Arrange
        val reading = CustomerOrdersReading(unknownCustomerId, PageRequest.of(0, 2))
        // Act
        val thrown = catchThrowable { listCustomerOrdersUseCase.unfold(reading) }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException::class.java)
    }

    @Test
    fun `an unknown customer should not read an order`() {
        // Arrange
        placeOrder(OrderId("order-1"), customer, placedAt)
        val lookup = CustomerOrderLookup(unknownCustomerId, OrderId("order-1"))
        // Act
        val thrown = catchThrowable { readCustomerOrderUseCase.unfold(lookup) }
        // Assert
        assertThat(thrown).isInstanceOf(UserNotFoundException::class.java)
    }

    @ParameterizedTest
    @EnumSource(AccountStatus::class, names = ["SUSPENDED", "LOCKED", "DISABLED"])
    fun `a customer whose account is refused should not list orders`(status: AccountStatus) {
        // Arrange
        userRepository.save(customerWithStatus(status))
        val reading = CustomerOrdersReading(blockedCustomerId, PageRequest.of(0, 2))
        // Act
        val thrown = catchThrowable { listCustomerOrdersUseCase.unfold(reading) }
        // Assert
        assertThat(thrown).isInstanceOf(AccountRefusedException::class.java)
    }

    @Test
    fun `a customer whose email is not verified yet should list orders`() {
        // Arrange
        userRepository.save(customerWithStatus(AccountStatus.PENDING_VERIFICATION))
        val reading = CustomerOrdersReading(blockedCustomerId, PageRequest.of(0, 2))
        // Act
        val page = listCustomerOrdersUseCase.unfold(reading)
        // Assert
        assertThat(page.content.asList()).isEmpty()
    }

    @Test
    fun `should list a page of the orders of the customer, newest first, with the total of their orders`() {
        // Arrange
        val oldest = placeOrder(OrderId("oldest"), customer, placedAt)
        val middle = placeOrder(OrderId("middle"), customer, placedAt.plusSeconds(60))
        val newest = placeOrder(OrderId("newest"), customer, placedAt.plusSeconds(120))
        placeOrder(OrderId("other"), otherCustomer, placedAt)
        val firstPage = CustomerOrdersReading(customer.userId, PageRequest.of(0, 2))
        val secondPage = CustomerOrdersReading(customer.userId, PageRequest.of(1, 2))
        // Act
        val first = listCustomerOrdersUseCase.unfold(firstPage)
        val second = listCustomerOrdersUseCase.unfold(secondPage)
        // Assert
        assertThat(first.content.asList()).containsExactly(newest, middle)
        assertThat(second.content.asList()).containsExactly(oldest)
        assertThat(first.total).isEqualTo(TotalCount(3))
    }
}
