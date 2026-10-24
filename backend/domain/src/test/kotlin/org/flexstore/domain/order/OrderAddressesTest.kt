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
    private val lamp = DefinedProduct(ProductId(Identity("lamp")), Name("Desk lamp"), Price(Amount(BigDecimal(35)), Currency.EUR))
    private val cart = Cart(customer).add(lamp, Quantity(1))
    private val office = PostalAddress(Street("3 place du Marché"), null, PostalCode("69001"), City("Lyon"), Country("France"))

    companion object {
        private val incomplete = home.copy(city = City(""))

        @JvmStatic
        fun incompleteAddresses() = listOf(
            Arguments.of("delivery", OrderAddresses(incomplete, home)),
            Arguments.of("billing", OrderAddresses(home, incomplete)),
        )
    }

    private fun placementWith(addresses: OrderAddresses) =
        OrderPlacement(OrderId("order-1"), customer, Instant.parse("2026-10-24T10:00:00Z"), addresses)

    @Test
    fun `an order should keep its delivery address and its billing address`() {
        // Act
        val order = Order.place(placementWith(OrderAddresses(home, office)), cart)
        // Assert
        assertThat(order.addresses).isEqualTo(OrderAddresses(delivery = home, billing = office))
    }

    @Test
    fun `an order should be billed to the delivery address when it is the same`() {
        // Act
        val order = Order.place(placementWith(OrderAddresses.sameFor(home)), cart)
        // Assert
        assertThat(order.addresses!!.isBilledToTheDeliveryAddress()).isTrue()
    }

    @ParameterizedTest(name = "an incomplete {0} address should be refused")
    @MethodSource("incompleteAddresses")
    fun `an order should not be placed with an incomplete address`(@Suppress("UNUSED_PARAMETER") which: String, addresses: OrderAddresses) {
        // Act
        val thrown = catchThrowable { Order.place(placementWith(addresses), cart) }
        // Assert
        assertThat(thrown).isInstanceOf(IncompleteOrderAddressException::class.java)
    }
}
