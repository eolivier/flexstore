package org.flexstore.domain.order.model

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.cart.*
import org.flexstore.domain.order.acceptedTermsOfSale
import org.flexstore.domain.order.homeOrderAddresses
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.UserId
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import java.math.BigDecimal
import java.time.Instant

class OrderTrackingTest : WithAssertions {

    private val deskLamp = DefinedProduct(
        ProductId(Identity("desk-lamp")), Name("Desk lamp"), Price(Amount(BigDecimal(35)), Currency.EUR)
    )
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val orderStamp = OrderStamp(OrderId("order-1"), Instant.parse("2026-10-24T10:00:00Z"))
    private val checkout = Checkout(customer, homeOrderAddresses, acceptedTermsOfSale)
    private val placedOrder = Order.place(
        OrderPlacement(orderStamp, checkout), Cart(customer).add(deskLamp, Quantity(1))
    )

    @ParameterizedTest
    @ValueSource(strings = ["6A12345678901", "1Z999AA10123456784", "AB-123-CD", "7"])
    fun `a tracking number of letters, digits and hyphens should be valid`(typedNumber: String) {
        // Act
        val trackingNumber = TrackingNumber.from(typedNumber)
        // Assert
        assertThat(trackingNumber.isValid()).isTrue()
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "   ", "AB 123", "AB_123", "AB#123", "é1234"])
    fun `a tracking number with another character, or empty, should be invalid`(typedNumber: String) {
        // Act
        val trackingNumber = TrackingNumber.from(typedNumber)
        // Assert
        assertThat(trackingNumber.isInvalid()).isTrue()
    }

    @ParameterizedTest
    @CsvSource("40, true", "41, false")
    fun `a tracking number should be at most the maximum length`(length: Int, valid: Boolean) {
        // Act
        val trackingNumber = TrackingNumber("A".repeat(length))
        // Assert
        assertThat(trackingNumber.isValid()).isEqualTo(valid)
    }

    @Test
    fun `a typed tracking number should lose the spaces around it`() {
        // Act
        val trackingNumber = TrackingNumber.from("  6A12345678901 ")
        // Assert
        assertThat(trackingNumber).isEqualTo(TrackingNumber("6A12345678901"))
    }

    @ParameterizedTest
    @CsvSource("colissimo, COLISSIMO", "Ups, UPS", " DHL , DHL", "other, OTHER")
    fun `a typed carrier should be known whatever its case`(typedCarrier: String, carrier: Carrier) {
        // Act
        val typed = Carrier.from(typedCarrier)
        // Assert
        assertThat(typed).isEqualTo(carrier)
    }

    @Test
    fun `an unknown carrier should be refused`() {
        // Act
        val thrown = catchThrowable { Carrier.from("pigeon") }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidOrderTrackingException::class.java)
    }

    @ParameterizedTest
    @CsvSource(
        "COLISSIMO, https://www.laposte.fr/outils/suivre-vos-envois?code=6A123",
        "CHRONOPOST, https://www.chronopost.fr/tracking-no-cms/suivi-page?listeNumerosLT=6A123",
        "UPS, https://www.ups.com/track?tracknum=6A123",
        "DHL, https://www.dhl.com/fr-fr/home/tracking.html?tracking-id=6A123",
        "FEDEX, https://www.fedex.com/fedextrack/?trknbr=6A123"
    )
    fun `a known carrier should give the tracking page of a parcel`(carrier: Carrier, page: String) {
        // Act
        val trackingPage = carrier.trackingPageOf(TrackingNumber("6A123"))
        // Assert
        assertThat(carrier.hasTrackingPage()).isTrue()
        assertThat(trackingPage).isEqualTo(TrackingPage(page))
    }

    @Test
    fun `another carrier should have no tracking page`() {
        // Act
        val hasTrackingPage = Carrier.OTHER.hasTrackingPage()
        // Assert
        assertThat(hasTrackingPage).isFalse()
    }

    @Test
    fun `the carrier and the number given together should be the tracking`() {
        // Act
        val tracking = OrderTracking.typed("ups", " 1Z999 ")
        // Assert
        assertThat(tracking).isEqualTo(OrderTracking.Given(Carrier.UPS, TrackingNumber("1Z999")))
    }

    @Test
    fun `neither the carrier nor the number should be no tracking`() {
        // Act
        val tracking = OrderTracking.typed("", "  ")
        // Assert
        assertThat(tracking).isEqualTo(OrderTracking.None)
    }

    @ParameterizedTest
    @CsvSource("ups, ''", "'', 1Z999")
    fun `the carrier without the number, or the number without the carrier, should be refused`(
        typedCarrier: String,
        typedNumber: String
    ) {
        // Act
        val thrown = catchThrowable { OrderTracking.typed(typedCarrier, typedNumber) }
        // Assert
        assertThat(thrown).isInstanceOf(InvalidOrderTrackingException::class.java)
    }

    @Test
    fun `an order should carry the tracking it is given, and keep it when none is given`() {
        // Arrange
        val tracking = OrderTracking.Given(Carrier.UPS, TrackingNumber("1Z999"))
        val trackedOrder = placedOrder.withTracking(tracking)
        // Act
        val untouchedOrder = trackedOrder.withTracking(OrderTracking.None)
        // Assert
        assertThat(trackedOrder.tracking).isEqualTo(tracking)
        assertThat(untouchedOrder.tracking).isEqualTo(tracking)
    }
}
