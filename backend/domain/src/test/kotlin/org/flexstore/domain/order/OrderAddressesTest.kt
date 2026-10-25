package org.flexstore.domain.order

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.math.BigDecimal
import java.time.Instant

class OrderAddressesTest : WithAssertions {

    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), Price(Amount(BigDecimal(35)), Currency.EUR))
    private val cart = Cart(customer).add(deskLamp, Quantity(1))

    companion object {
        private val nameless = LegalEntity(Company(CompanyName("")))
        private val incompleteAddress = homeAddress.copy(location = homeLocation.copy(city = City("")))

        @JvmStatic
        fun incompleteAddresses() = listOf(
            Arguments.of("delivery", OrderAddresses(incompleteAddress, homeAddress)),
            Arguments.of("billing", OrderAddresses(homeAddress, incompleteAddress)),
            Arguments.of("company", OrderAddresses(homeAddress, PostalAddress(nameless, homeLocation))),
        )
    }

    private fun orderPlacementWith(orderAddresses: OrderAddresses) =
        OrderPlacement(OrderStamp(OrderId("order-1"), Instant.parse("2026-10-24T10:00:00Z")), Checkout(customer, orderAddresses))

    @Test
    fun `an order should keep its delivery address and its billing address`() {
        // Arrange
        val orderPlacement = orderPlacementWith(OrderAddresses(homeAddress, officeAddress))
        // Act
        val order = Order.place(orderPlacement, cart)
        // Assert
        assertThat(order.addresses).isEqualTo(OrderAddresses(delivery = homeAddress, billing = officeAddress))
    }

    @Test
    fun `an order should be billed to a company, without the name of a person`() {
        // Arrange
        val acme = LegalEntity(Company(CompanyName("Acme"), VatNumber("FR12345678901")))
        val orderPlacement = orderPlacementWith(OrderAddresses(homeAddress, PostalAddress(acme, homeLocation)))
        // Act
        val order = Order.place(orderPlacement, cart)
        // Assert
        assertThat(order.addresses.billing.recipient).isEqualTo(acme)
    }

    @Test
    fun `an order should be billed to the delivery address when it is the same`() {
        // Arrange
        val orderPlacement = orderPlacementWith(OrderAddresses.sameFor(homeAddress))
        // Act
        val order = Order.place(orderPlacement, cart)
        // Assert
        assertThat(order.addresses.isBilledToTheDeliveryAddress()).isTrue()
    }

    @ParameterizedTest(name = "an incomplete {0} address should be refused")
    @MethodSource("incompleteAddresses")
    fun `an order should not be placed with an incomplete address`(@Suppress("UNUSED_PARAMETER") which: String, orderAddresses: OrderAddresses) {
        // Arrange
        val orderPlacement = orderPlacementWith(orderAddresses)
        // Act
        val thrown = catchThrowable { Order.place(orderPlacement, cart) }
        // Assert
        assertThat(thrown).isInstanceOf(IncompleteOrderAddressException::class.java)
    }
}
