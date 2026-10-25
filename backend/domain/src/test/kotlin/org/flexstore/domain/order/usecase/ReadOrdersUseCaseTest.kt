package org.flexstore.domain.order.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.assertj.core.api.WithAssertions
import org.flexstore.infra.repository.InMemoryOrderRepository
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class ReadOrdersUseCaseTest : WithAssertions {

    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), Price(Amount(BigDecimal(35)), Currency.EUR))
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val otherCustomer = CartOwner.Customer(UserId.ValidUserId("user-2"))
    private val placedAt = Instant.parse("2026-10-24T10:00:00Z")
    private val orderRepository = InMemoryOrderRepository()
    private val useCase = ReadOrdersUseCase(orderRepository)

    private fun placeOrder(orderId: OrderId, customer: CartOwner.Customer, placedAt: Instant): Order {
        val orderStamp = OrderStamp(orderId, placedAt)
        val checkout = Checkout(customer, homeOrderAddresses)
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
        val orders = useCase.getOrders(customer.userId)
        // Assert
        assertThat(orders.asList()).containsExactly(newer, older)
    }

    @Test
    fun `should read an order of the customer`() {
        // Arrange
        val order = placeOrder(OrderId("order-1"), customer, placedAt)
        // Act
        val readOrder = useCase.getOrder(customer.userId, OrderId("order-1"))
        // Assert
        assertThat(readOrder).isEqualTo(order)
    }

    @Test
    fun `the order of another customer should not be found`() {
        // Arrange
        placeOrder(OrderId("other"), otherCustomer, placedAt)
        // Act
        val thrown = catchThrowable { useCase.getOrder(customer.userId, OrderId("other")) }
        // Assert
        assertThat(thrown).isInstanceOf(OrderNotFoundException::class.java)
    }
}
