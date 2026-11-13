package org.flexstore.domain.email.model

import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
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
    private val deskLamp = DefinedProduct(ProductId("desk-lamp"), Name("Desk lamp"), lampPrice)
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val orderStamp = OrderStamp(OrderId("46555c9f-4e92"), Instant.parse("2026-10-24T10:00:00Z"))
    private val checkout = Checkout(customer, homeOrderAddresses, acceptedTermsOfSale)
    private val orderPlacement = OrderPlacement(orderStamp, checkout)
    private val placedOrder = Order.place(orderPlacement, Cart(customer).add(deskLamp, Quantity(1)))
    private val janeDoe = FullName(FirstName("Jane"), LastName("Doe"))
    private val shopUrl = ShopUrl("https://flexstore.example")

    private fun contentFor(status: OrderStatus, tracking: OrderTracking = OrderTracking.None):
        OrderStatusEmailContent {
        val order = placedOrder.withStatus(status).withTracking(tracking)
        val email = OrderStatusEmail(Email("jane@example.com"), janeDoe, order)
        return OrderStatusEmailContent(email, shopUrl)
    }

    @ParameterizedTest(name = "an order {0} is announced as \"{1}\"")
    @CsvSource("SHIPPED, a été expédiée", "DELIVERED, a été livrée", "REFUNDED, a été annulée et remboursée")
    fun `the subject should give the number of the order and its news`(status: OrderStatus, news: String) {
        // Act
        val content = contentFor(status)
        // Assert
        assertThat(content.subject.value).isEqualTo("Flexstore - Votre commande 46555C9F $news")
    }

    @Test
    fun `the email of a refunded order should say that the payment is given back`() {
        // Act
        val content = contentFor(OrderStatus.REFUNDED)
        // Assert
        assertThat(content.body.value).contains("Le montant payé vous est remboursé")
    }

    @Test
    fun `the email should greet the customer and give the page of the order`() {
        // Act
        val content = contentFor(OrderStatus.SHIPPED)
        // Assert
        assertThat(content.body.value).startsWith("Bonjour Jane Doe,")
        assertThat(content.body.value).contains("https://flexstore.example/orders/46555c9f-4e92")
    }

    @Test
    fun `the email of a shipped order should give the carrier, the number and the tracking page`() {
        // Arrange
        val tracking = OrderTracking.Given(Carrier.COLISSIMO, TrackingNumber("6A12345678901"))
        // Act
        val content = contentFor(OrderStatus.SHIPPED, tracking)
        // Assert
        assertThat(content.body.value).contains("Transporteur : Colissimo")
        assertThat(content.body.value).contains("Numéro de suivi : 6A12345678901")
        assertThat(content.body.value)
            .contains("Suivre le colis : https://www.laposte.fr/outils/suivre-vos-envois?code=6A12345678901")
    }

    @Test
    fun `the email should give the number without a tracking page for another carrier`() {
        // Arrange
        val tracking = OrderTracking.Given(Carrier.OTHER, TrackingNumber("ABC-123"))
        // Act
        val content = contentFor(OrderStatus.SHIPPED, tracking)
        // Assert
        assertThat(content.body.value).contains("Transporteur : Autre transporteur")
        assertThat(content.body.value).contains("Numéro de suivi : ABC-123")
        assertThat(content.body.value).doesNotContain("Suivre le colis")
    }

    @Test
    fun `the email of a shipped order without tracking should not mention a carrier`() {
        // Act
        val content = contentFor(OrderStatus.SHIPPED)
        // Assert
        assertThat(content.body.value).doesNotContain("Transporteur").doesNotContain("Numéro de suivi")
    }

    @Test
    fun `the email of a delivered order should not repeat the tracking`() {
        // Arrange
        val tracking = OrderTracking.Given(Carrier.UPS, TrackingNumber("1Z999"))
        // Act
        val content = contentFor(OrderStatus.DELIVERED, tracking)
        // Assert
        assertThat(content.body.value).doesNotContain("Numéro de suivi")
    }
}
