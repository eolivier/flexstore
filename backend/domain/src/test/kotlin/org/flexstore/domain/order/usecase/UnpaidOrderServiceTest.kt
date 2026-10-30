package org.flexstore.domain.order.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.payment.PaymentOutcome
import org.flexstore.domain.product.*
import org.flexstore.domain.settings.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.stock.*
import org.flexstore.domain.user.model.UserId
import org.flexstore.infra.repository.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class UnpaidOrderServiceTest : WithAssertions {

    private val placedAt = Instant.parse("2026-10-25T10:00:00Z")
    private val price = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), price)
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val orderId = OrderId("order-1")
    private val orderRepository = InMemoryOrderRepository()
    // 8 units left once the order reserved 2
    private val deskLampStock = Stock(deskLamp.productId, StockQuantity(8))
    private val stockRepository = InMemoryStockRepository().apply { save(deskLampStock) }
    private val paymentRepository = InMemoryPaymentRepository(PaymentOutcome.PENDING)
    private val shopSettingsRepository = InMemoryShopSettingsRepository().apply {
        save(ShopSettings.byDefault().withPaymentDelay(PaymentDelay(10)))
    }
    private val cancelUnpaidOrderUseCase =
        CancelUnpaidOrderUseCase(orderRepository, stockRepository, paymentRepository)

    private fun serviceAt(instant: Instant) = UnpaidOrderService(
        ReadShopSettingsUseCase(shopSettingsRepository),
        ListAllOrdersUseCase(orderRepository),
        cancelUnpaidOrderUseCase,
        Clock.fixed(instant, ZoneOffset.UTC)
    )

    private fun placeOrder() {
        val cart = Cart(customer).add(deskLamp, Quantity(2))
        val checkout = Checkout(customer, homeOrderAddresses)
        val orderPlacement = OrderPlacement(OrderStamp(orderId, placedAt), checkout)
        orderRepository.save(Order.place(orderPlacement, cart))
    }

    private fun storedStatus() = orderRepository.findById(orderId)?.status

    @Test
    fun `an order not paid within the payment delay should be cancelled, and its payment stopped`() {
        // Arrange
        placeOrder()
        // Act
        serviceAt(placedAt.plusSeconds(10 * 60)).cancelLateOrders()
        // Assert
        assertThat(storedStatus()).isEqualTo(OrderStatus.CANCELLED)
        assertThat(paymentRepository.isStopped(orderId)).isTrue()
    }

    @Test
    fun `a cancelled order should give its units back to the stock`() {
        // Arrange
        placeOrder()
        // Act
        serviceAt(placedAt.plusSeconds(10 * 60)).cancelLateOrders()
        // Assert
        assertThat(stockRepository.stockOf(deskLamp.productId).quantity).isEqualTo(StockQuantity(10))
    }

    @Test
    fun `an order within the payment delay should still await its payment`() {
        // Arrange
        placeOrder()
        // Act
        serviceAt(placedAt.plusSeconds(10 * 60 - 1)).cancelLateOrders()
        // Assert
        assertThat(storedStatus()).isEqualTo(OrderStatus.AWAITING_PAYMENT)
    }

    @Test
    fun `an order whose customer has just paid should not be cancelled`() {
        // Arrange
        placeOrder()
        paymentRepository.completePaymentOf(orderId)
        // Act
        val cancelledOrders = serviceAt(placedAt.plusSeconds(10 * 60)).cancelLateOrders()
        // Assert
        assertThat(cancelledOrders.asList()).isEmpty()
        assertThat(storedStatus()).isEqualTo(OrderStatus.AWAITING_PAYMENT)
        assertThat(stockRepository.stockOf(deskLamp.productId).quantity).isEqualTo(StockQuantity(8))
    }

    @Test
    fun `a paid order should never be cancelled`() {
        // Arrange
        placeOrder()
        orderRepository.save(orderRepository.findById(orderId)!!.withStatus(OrderStatus.PAID))
        // Act
        serviceAt(placedAt.plusSeconds(3600)).cancelLateOrders()
        // Assert
        assertThat(storedStatus()).isEqualTo(OrderStatus.PAID)
    }
}
