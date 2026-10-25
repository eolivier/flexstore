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
    private val officeAddress = PostalAddress(Street("3 place du Marché"), null, PostalCode("69001"), City("Lyon"), Country("France"))

    companion object {
        private val incompleteAddress = homeAddress.copy(city = City(""))

        @JvmStatic
        fun incompleteAddresses() = listOf(
            Arguments.of("delivery", OrderAddresses(incompleteAddress, homeAddress)),
            Arguments.of("billing", OrderAddresses(homeAddress, incompleteAddress)),
        )
    }

    private fun orderPlacementWith(orderAddresses: OrderAddresses) =
        OrderPlacement(OrderId("order-1"), customer, Instant.parse("2026-10-24T10:00:00Z"), orderAddresses)

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
