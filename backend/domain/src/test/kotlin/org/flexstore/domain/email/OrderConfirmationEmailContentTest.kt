package org.flexstore.domain.email

import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.assertj.core.api.WithAssertions
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class OrderConfirmationEmailContentTest : WithAssertions {

    private val lampPrice = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), lampPrice)
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val orderStamp = OrderStamp(OrderId("46555c9f-4e92"), Instant.parse("2026-10-24T10:00:00Z"))
    private val janeDoe = FullName(FirstName("Jane"), LastName("Doe"))
    private val shopUrl = ShopUrl("https://flexstore.example")

    private fun contentOf(shippingMethod: ShippingMethod, quantity: Int): OrderConfirmationEmailContent {
        val delivery = OrderDelivery(homeOrderAddresses, shippingMethod)
        val orderPlacement = OrderPlacement(orderStamp, Checkout(customer, delivery))
        val order = Order.place(orderPlacement, Cart(customer).add(deskLamp, Quantity(quantity)))
        return OrderConfirmationEmailContent(OrderConfirmationEmail(Email("jane@example.com"), janeDoe, order), shopUrl)
    }

    @Test
    fun `the email should give the shipping and the total that includes it`() {
        // Act
        val content = contentOf(ShippingMethod.EXPRESS, 1)
        // Assert
        assertThat(content.body.value).contains("- Livraison express : 9,90 €")
        assertThat(content.body.value).contains("Total : 44,90 €")
    }

    @Test
    fun `a free shipping should be said so`() {
        // Act
        val content = contentOf(ShippingMethod.STANDARD, 2)
        // Assert
        assertThat(content.body.value).contains("- Livraison standard : offerte")
        assertThat(content.body.value).contains("Total : 70,00 €")
    }
}
