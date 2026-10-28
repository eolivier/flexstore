package org.flexstore.domain.order.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.assertj.core.api.WithAssertions
import org.flexstore.infra.repository.InMemoryOrderRepository
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class ReadAdministeredOrdersUseCaseTest : WithAssertions {

    private val lampPrice = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), lampPrice)
    private val janeDoe = FullName(FirstName("Jane"), LastName("Doe"))
    private val janeId = UserId.ValidUserId("user-1")
    private val jane = User.DefinedUser(janeId, janeDoe, Email("jane@example.com"), PlainPassword("password123"))
    private val customer = CartOwner.Customer(janeId)
    private val formerCustomer = CartOwner.Customer(UserId.ValidUserId("deleted-user"))
    private val placedAt = Instant.parse("2026-10-24T10:00:00Z")
    private val orderRepository = InMemoryOrderRepository()
    private val userRepository = InMemoryUserRepository().apply { save(jane) }
    private val useCase = ReadAdministeredOrdersUseCase(orderRepository, userRepository)

    private fun placeOrder(orderId: OrderId, customer: CartOwner.Customer, placedAt: Instant): Order {
        val orderStamp = OrderStamp(orderId, placedAt)
        val orderPlacement = OrderPlacement(orderStamp, Checkout(customer, homeOrderAddresses))
        val cart = Cart(customer).add(deskLamp, Quantity(1))
        return orderRepository.save(Order.place(orderPlacement, cart))
    }

    @Test
    fun `should read an order with the customer who placed it`() {
        // Arrange
        val order = placeOrder(OrderId("order-1"), customer, placedAt)
        // Act
        val administeredOrder = useCase.getOrder(OrderId("order-1"))
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
        val administeredOrders = useCase.getAllOrders()
        // Assert
        val janeAsCustomer = OrderCustomer(janeDoe, Email("jane@example.com"))
        assertThat(administeredOrders.asList()).containsExactly(
            AdministeredOrder(newer, OrderCustomer.unknown()),
            AdministeredOrder(older, janeAsCustomer)
        )
    }

    @Test
    fun `an order whose customer cannot be read should have an unknown customer`() {
        // Arrange
        placeOrder(OrderId("order-1"), formerCustomer, placedAt)
        // Act
        val administeredOrder = useCase.getOrder(OrderId("order-1"))
        // Assert
        assertThat(administeredOrder.customer).isEqualTo(OrderCustomer.unknown())
    }

    @Test
    fun `should report an unknown order`() {
        // Act
        val thrown = catchThrowable { useCase.getOrder(OrderId("unknown")) }
        // Assert
        assertThat(thrown).isInstanceOf(OrderNotFoundException::class.java)
    }
}
