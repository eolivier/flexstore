package org.flexstore.domain.payment

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class PayerTest : WithAssertions {

    private val price = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), price)
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val janeDoe = FullName(FirstName("Jane"), LastName("Doe"))
    private val johnSmith = FullName(FirstName("John"), LastName("Smith"))
    private val email = Email("jane@example.com")
    private val definedUser = User.DefinedUser(customer.userId, janeDoe, email, PlainPassword("pwd"))
    private val acme = Company(CompanyName("Acme"))

    private fun orderTo(orderAddresses: OrderAddresses): Order {
        val orderStamp = OrderStamp(OrderId("order-1"), Instant.parse("2026-10-24T10:00:00Z"))
        val orderPlacement = OrderPlacement(orderStamp, Checkout(customer, orderAddresses))
        return Order.place(orderPlacement, Cart(customer).add(deskLamp, Quantity(1)))
    }

    @Test
    fun `a person should pay with the card of the signed-in customer`() {
        // Arrange
        val order = orderTo(homeOrderAddresses)
        // Act
        val payer = Payer.of(definedUser, order)
        // Assert
        assertThat(payer).isEqualTo(Payer(email, Cardholder.Named(janeDoe)))
    }

    @Test
    fun `a company should pay with the card of the person given for the order`() {
        // Arrange
        val deliveredToJohnAtAcme = PostalAddress(LegalEntity(acme, johnSmith), homeLocation)
        val billedToAcme = PostalAddress(LegalEntity(acme), homeLocation)
        val order = orderTo(OrderAddresses(deliveredToJohnAtAcme, billedToAcme))
        // Act
        val payer = Payer.of(definedUser, order)
        // Assert
        assertThat(payer.cardholder).isEqualTo(Cardholder.Named(johnSmith))
    }

    @Test
    fun `a company without any person for the order should leave the name of the cardholder empty`() {
        // Arrange
        val order = orderTo(OrderAddresses.sameFor(PostalAddress(LegalEntity(acme), homeLocation)))
        // Act
        val payer = Payer.of(definedUser, order)
        // Assert
        assertThat(payer).isEqualTo(Payer(email, Cardholder.Unknown))
    }
}
