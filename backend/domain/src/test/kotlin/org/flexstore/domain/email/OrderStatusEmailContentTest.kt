package org.flexstore.domain.email

import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import java.time.Instant

class OrderStatusEmailContentTest : WithAssertions {

    private val lampPrice = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), lampPrice)
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val orderStamp = OrderStamp(OrderId("46555c9f-4e92"), Instant.parse("2026-10-24T10:00:00Z"))
    private val orderPlacement = OrderPlacement(orderStamp, Checkout(customer, homeOrderAddresses))
    private val placedOrder = Order.place(orderPlacement, Cart(customer).add(deskLamp, Quantity(1)))
    private val janeDoe = FullName(FirstName("Jane"), LastName("Doe"))
    private val shopUrl = ShopUrl("https://flexstore.example")

    private fun contentFor(status: OrderStatus): OrderStatusEmailContent {
        val email = OrderStatusEmail(Email("jane@example.com"), janeDoe, placedOrder.copy(status = status))
        return OrderStatusEmailContent(email, shopUrl)
    }

    @ParameterizedTest(name = "an order {0} is announced as \"{1}\"")
    @CsvSource("SHIPPED, a été expédiée", "DELIVERED, a été livrée")
    fun `the subject should give the number of the order and its news`(status: OrderStatus, news: String) {
        // Act
        val content = contentFor(status)
        // Assert
        assertThat(content.subject.value).isEqualTo("Flexstore - Votre commande 46555C9F $news")
    }

    @Test
    fun `the email should greet the customer and give the page of the order`() {
        // Act
        val content = contentFor(OrderStatus.SHIPPED)
        // Assert
        assertThat(content.body.value).startsWith("Bonjour Jane Doe,")
        assertThat(content.body.value).contains("https://flexstore.example/orders/46555c9f-4e92")
    }
}
