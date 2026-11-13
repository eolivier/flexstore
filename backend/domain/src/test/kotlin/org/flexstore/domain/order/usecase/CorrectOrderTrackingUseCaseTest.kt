package org.flexstore.domain.order.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.actor.unknownUserActor
import org.flexstore.domain.actor.testCustomerActor
import org.flexstore.domain.actor.testAdministratorActor
import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.flexstore.infra.repository.InMemoryOrderRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.math.BigDecimal
import java.time.Instant

class CorrectOrderTrackingUseCaseTest : WithAssertions {

    private val price = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), price)
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val orderId = OrderId("order-1")
    private val orderRepository = InMemoryOrderRepository()
    private val administrator = testAdministratorActor
    private val useCase = CorrectOrderTrackingUseCase(orderRepository)
    private val firstTracking = OrderTracking.Given(Carrier.COLISSIMO, TrackingNumber("6A111"))
    private val correctedTracking = OrderTracking.Given(Carrier.UPS, TrackingNumber("1Z222"))

    private fun storeOrder(status: OrderStatus, tracking: OrderTracking = OrderTracking.None) {
        val orderStamp = OrderStamp(orderId, Instant.parse("2026-10-24T10:00:00Z"))
        val checkout = Checkout(customer, homeOrderAddresses, acceptedTermsOfSale)
        val order = Order.place(OrderPlacement(orderStamp, checkout), Cart(customer).add(deskLamp, Quantity(1)))
        orderRepository.save(order.withStatus(status).withTracking(tracking))
    }

    private fun storedTracking() = orderRepository.findById(orderId)?.tracking

    @ParameterizedTest(name = "the tracking of an order {0} should be corrected")
    @CsvSource("SHIPPED", "DELIVERED")
    fun `an administrator should correct the tracking of a shipped or delivered order`(status: OrderStatus) {
        // Arrange
        storeOrder(status, firstTracking)
        // Act
        useCase.unfold(OrderTrackingCorrection(administrator, orderId, correctedTracking))
        // Assert
        assertThat(storedTracking()).isEqualTo(correctedTracking)
    }

    @Test
    fun `a shipped order shipped without tracking should be given one`() {
        // Arrange
        storeOrder(OrderStatus.SHIPPED)
        // Act
        useCase.unfold(OrderTrackingCorrection(administrator, orderId, correctedTracking))
        // Assert
        assertThat(storedTracking()).isEqualTo(correctedTracking)
    }

    @ParameterizedTest(name = "the tracking of an order {0} should not be corrected")
    @CsvSource("AWAITING_PAYMENT", "PAID", "PROCESSING", "CANCELLED", "REFUNDED")
    fun `the tracking of an order not shipped yet, cancelled or refunded should not be corrected`(
        status: OrderStatus
    ) {
        // Arrange
        storeOrder(status)
        // Act
        val thrown = catchThrowable {
            useCase.unfold(OrderTrackingCorrection(administrator, orderId, correctedTracking))
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(OrderTrackingCannotBeCorrectedException::class.java)
        assertThat(storedTracking()).isEqualTo(OrderTracking.None)
    }

    @Test
    fun `an invalid tracking number should be refused`() {
        // Arrange
        storeOrder(OrderStatus.SHIPPED, firstTracking)
        val invalidTracking = OrderTracking.Given(Carrier.UPS, TrackingNumber("1Z 222"))
        // Act
        val thrown = catchThrowable {
            useCase.unfold(OrderTrackingCorrection(administrator, orderId, invalidTracking))
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(InvalidOrderTrackingException::class.java)
        assertThat(storedTracking()).isEqualTo(firstTracking)
    }

    @Test
    fun `an unknown order should not be found`() {
        // Act
        val thrown = catchThrowable {
            useCase.unfold(OrderTrackingCorrection(administrator, orderId, correctedTracking))
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(OrderNotFoundException::class.java)
    }

    @Test
    fun `a customer should not correct a tracking`() {
        // Arrange
        storeOrder(OrderStatus.SHIPPED, firstTracking)
        // Act
        val thrown = catchThrowable {
            useCase.unfold(OrderTrackingCorrection(testCustomerActor, orderId, correctedTracking))
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(OrderAdministrationForbiddenException::class.java)
        assertThat(storedTracking()).isEqualTo(firstTracking)
    }

    @Test
    fun `an unknown user should not correct a tracking`() {
        // Arrange
        storeOrder(OrderStatus.SHIPPED, firstTracking)
        // Act
        val thrown = catchThrowable {
            useCase.unfold(OrderTrackingCorrection(unknownUserActor, orderId, correctedTracking))
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(OrderAdministrationForbiddenException::class.java)
    }
}
