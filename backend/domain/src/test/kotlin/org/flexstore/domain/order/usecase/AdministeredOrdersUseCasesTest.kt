package org.flexstore.domain.order.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.assertj.core.api.WithAssertions
import org.flexstore.infra.repository.InMemoryOrderRepository
import org.flexstore.infra.repository.InMemoryUserRepository
import org.flexstore.domain.user.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class AdministeredOrdersUseCasesTest : WithAssertions {

    private val lampPrice = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), lampPrice)
    private val janeDoe = FullName(FirstName("Jane"), LastName("Doe"))
    private val janeId = UserId.ValidUserId("user-1")
    private val jane = User.DefinedUser(janeId, janeDoe, Email("jane@example.com"), PlainPassword("password123"))
    private val customer = CartOwner.Customer(janeId)
    private val formerCustomer = CartOwner.Customer(UserId.ValidUserId("deleted-user"))
    private val placedAt = Instant.parse("2026-10-24T10:00:00Z")
    private val orderRepository = InMemoryOrderRepository()
    private val userRepository = usersOfTheAdministration().apply { save(jane) }
    private val administrator = testAdministrator.id
    private val listUseCase = ListAdministeredOrdersUseCase(orderRepository, userRepository)
    private val readUseCase = ReadAdministeredOrderUseCase(orderRepository, userRepository)

    private fun placeOrder(orderId: OrderId, customer: CartOwner.Customer, placedAt: Instant): Order {
        val orderStamp = OrderStamp(orderId, placedAt)
        val checkout = Checkout(customer, homeOrderAddresses, acceptedTermsOfSale)
        val orderPlacement = OrderPlacement(orderStamp, checkout)
        val cart = Cart(customer).add(deskLamp, Quantity(1))
        return orderRepository.save(Order.place(orderPlacement, cart))
    }

    @Test
    fun `should read an order with the customer who placed it`() {
        // Arrange
        val order = placeOrder(OrderId("order-1"), customer, placedAt)
        // Act
        val administeredOrder = readUseCase.unfold(AdministeredOrderReading(administrator, OrderId("order-1")))
        // Assert
        val janeAsCustomer = OrderCustomer(janeDoe, Email("jane@example.com"))
        assertThat(administeredOrder).isEqualTo(AdministeredOrder(order, janeAsCustomer))
    }

    @Test
    fun `should list the orders of all the customers with their customer, newest first`() {
        // Arrange
        val older = placeOrder(OrderId("older"), customer, placedAt)
        val newer = placeOrder(OrderId("newer"), formerCustomer, placedAt.plusSeconds(60))
        // Act
        val reading = AdministeredOrdersReading(administrator, PageRequest.first())
        val administeredOrders = listUseCase.unfold(reading)
        // Assert
        val janeAsCustomer = OrderCustomer(janeDoe, Email("jane@example.com"))
        assertThat(administeredOrders.content.asList()).containsExactly(
            AdministeredOrder(newer, OrderCustomer.unknown()),
            AdministeredOrder(older, janeAsCustomer)
        )
    }

    @Test
    fun `an order whose customer cannot be read should have an unknown customer`() {
        // Arrange
        placeOrder(OrderId("order-1"), formerCustomer, placedAt)
        // Act
        val administeredOrder = readUseCase.unfold(AdministeredOrderReading(administrator, OrderId("order-1")))
        // Assert
        assertThat(administeredOrder.customer).isEqualTo(OrderCustomer.unknown())
    }

    @Test
    fun `should report an unknown order`() {
        // Act
        val thrown = catchThrowable { readUseCase.unfold(AdministeredOrderReading(administrator, OrderId("unknown"))) }
        // Assert
        assertThat(thrown).isInstanceOf(OrderNotFoundException::class.java)
    }

    @Test
    fun `a customer should not read the orders of all the customers`() {
        // Act
        val reading = AdministeredOrdersReading(testCustomer.id, PageRequest.first())
        val thrown = catchThrowable { listUseCase.unfold(reading) }
        // Assert
        assertThat(thrown).isInstanceOf(OrderAdministrationForbiddenException::class.java)
    }

    @Test
    fun `a customer should read their own order with their name and email`() {
        // Arrange
        val order = placeOrder(OrderId("order-1"), customer, placedAt)
        // Act
        val administeredOrder = readUseCase.unfold(AdministeredOrderReading(janeId, OrderId("order-1")))
        // Assert
        assertThat(administeredOrder).isEqualTo(AdministeredOrder(order, OrderCustomer(janeDoe, Email("jane@example.com"))))
    }

    @Test
    fun `the order of another customer should not be found`() {
        // Arrange
        placeOrder(OrderId("order-1"), customer, placedAt)
        // Act
        val thrown = catchThrowable { readUseCase.unfold(AdministeredOrderReading(testCustomer.id, OrderId("order-1"))) }
        // Assert
        assertThat(thrown).isInstanceOf(OrderNotFoundException::class.java)
    }

    @Test
    fun `an unknown user should not find any order`() {
        // Arrange
        placeOrder(OrderId("order-1"), customer, placedAt)
        // Act
        val thrown = catchThrowable { readUseCase.unfold(AdministeredOrderReading(unknownUser, OrderId("order-1"))) }
        // Assert
        assertThat(thrown).isInstanceOf(OrderNotFoundException::class.java)
    }

    @Test
    fun `should list a page of the orders, newest first, with the total of all the orders`() {
        // Arrange
        placeOrder(OrderId("oldest"), customer, placedAt)
        val middle = placeOrder(OrderId("middle"), customer, placedAt.plusSeconds(60))
        val newest = placeOrder(OrderId("newest"), customer, placedAt.plusSeconds(120))
        // Act
        val page = listUseCase.unfold(AdministeredOrdersReading(administrator, PageRequest.of(0, 2)))
        // Assert
        assertThat(page.content.asList().map { it.order }).containsExactly(newest, middle)
        assertThat(page.total).isEqualTo(TotalCount(3))
    }

    @Test
    fun `should list the last page with the orders left`() {
        // Arrange
        val oldest = placeOrder(OrderId("oldest"), customer, placedAt)
        placeOrder(OrderId("middle"), customer, placedAt.plusSeconds(60))
        placeOrder(OrderId("newest"), customer, placedAt.plusSeconds(120))
        // Act
        val page = listUseCase.unfold(AdministeredOrdersReading(administrator, PageRequest.of(1, 2)))
        // Assert
        assertThat(page.content.asList().map { it.order }).containsExactly(oldest)
        assertThat(page.total).isEqualTo(TotalCount(3))
    }

    @Test
    fun `a page after the last one should be empty`() {
        // Arrange
        placeOrder(OrderId("order-1"), customer, placedAt)
        // Act
        val page = listUseCase.unfold(AdministeredOrdersReading(administrator, PageRequest.of(5, 2)))
        // Assert
        assertThat(page.content.asList()).isEmpty()
        assertThat(page.total).isEqualTo(TotalCount(1))
    }
}
