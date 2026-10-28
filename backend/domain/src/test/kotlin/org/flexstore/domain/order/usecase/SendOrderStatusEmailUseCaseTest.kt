package org.flexstore.domain.order.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.email.*
import org.flexstore.domain.order.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.assertj.core.api.WithAssertions
import org.flexstore.infra.repository.InMemoryEmailRepository
import org.flexstore.infra.repository.InMemoryUserRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import java.time.Instant

class SendOrderStatusEmailUseCaseTest : WithAssertions {

    private val lampPrice = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), lampPrice)
    private val janeDoe = FullName(FirstName("Jane"), LastName("Doe"))
    private val janeId = UserId.ValidUserId("user-1")
    private val jane = User.DefinedUser(janeId, janeDoe, Email("jane@example.com"), PlainPassword("password123"))
    private val userRepository = InMemoryUserRepository().apply { save(jane) }
    private val emailRepository = InMemoryEmailRepository()
    private val useCase = SendOrderStatusEmailUseCase(userRepository, emailRepository)

    private fun orderOf(customerId: UserId.ValidUserId, status: OrderStatus): Order {
        val customer = CartOwner.Customer(customerId)
        val orderStamp = OrderStamp(OrderId("order-1"), Instant.parse("2026-10-24T10:00:00Z"))
        val orderPlacement = OrderPlacement(orderStamp, Checkout(customer, homeOrderAddresses))
        val placedOrder = Order.place(orderPlacement, Cart(customer).add(deskLamp, Quantity(1)))
        return placedOrder.copy(status = status)
    }

    @ParameterizedTest(name = "an order {0} sends {1} email(s)")
    @CsvSource("PAID, 0", "PROCESSING, 0", "SHIPPED, 1", "DELIVERED, 1", "CANCELLED, 0")
    fun `the customer should be told when their order is shipped, then delivered`(
        status: OrderStatus,
        sentCount: Int
    ) {
        // Arrange
        val order = orderOf(janeId, status)
        // Act
        useCase.unfold(order)
        // Assert
        assertThat(emailRepository.sentOrderStatusEmails()).hasSize(sentCount)
    }

    @Test
    fun `the email should be sent to the customer, with the order`() {
        // Arrange
        val shippedOrder = orderOf(janeId, OrderStatus.SHIPPED)
        // Act
        useCase.unfold(shippedOrder)
        // Assert
        val expectedEmail = OrderStatusEmail(Email("jane@example.com"), janeDoe, shippedOrder)
        assertThat(emailRepository.sentOrderStatusEmails()).containsExactly(expectedEmail)
    }

    @Test
    fun `no email should be sent when the customer cannot be read any more`() {
        // Arrange
        val shippedOrder = orderOf(UserId.ValidUserId("deleted-user"), OrderStatus.SHIPPED)
        // Act
        useCase.unfold(shippedOrder)
        // Assert
        assertThat(emailRepository.sentOrderStatusEmails()).isEmpty()
    }
}
