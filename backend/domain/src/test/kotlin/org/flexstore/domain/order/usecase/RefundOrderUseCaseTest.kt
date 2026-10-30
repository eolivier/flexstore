package org.flexstore.domain.order.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.payment.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.stock.*
import org.flexstore.domain.user.model.UserId
import org.flexstore.infra.repository.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import java.math.BigDecimal
import java.time.Instant

class RefundOrderUseCaseTest : WithAssertions {

    private val price = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), price)
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val orderId = OrderId("order-1")
    private val orderRepository = InMemoryOrderRepository()
    // 8 units left once the order reserved 2
    private val stockRepository = InMemoryStockRepository().apply {
        save(Stock(deskLamp.productId, StockQuantity(8)))
    }
    private val paymentRepository = InMemoryPaymentRepository(PaymentOutcome.PAID)
    private val useCase = RefundOrderUseCase(orderRepository, stockRepository, paymentRepository)

    private fun storeOrder(status: OrderStatus) {
        val cart = Cart(customer).add(deskLamp, Quantity(2))
        val orderStamp = OrderStamp(orderId, Instant.now())
        val orderPlacement = OrderPlacement(orderStamp, Checkout(customer, homeOrderAddresses))
        orderRepository.save(Order.place(orderPlacement, cart).withStatus(status))
    }

    private fun storedStatus() = orderRepository.findById(orderId)?.status

    @ParameterizedTest(name = "an order {0} is refunded")
    @EnumSource(OrderStatus::class, names = ["PAID", "PROCESSING"])
    fun `a paid order not shipped yet should be refunded`(status: OrderStatus) {
        // Arrange
        storeOrder(status)
        // Act
        useCase.unfold(OrderRefund(orderId, status))
        // Assert
        assertThat(storedStatus()).isEqualTo(OrderStatus.REFUNDED)
        assertThat(paymentRepository.isRefunded(orderId)).isTrue()
    }

    @Test
    fun `the units of a refunded order should go back to the stock`() {
        // Arrange
        storeOrder(OrderStatus.PAID)
        // Act
        useCase.unfold(OrderRefund(orderId, OrderStatus.PAID))
        // Assert
        assertThat(stockRepository.stockOf(deskLamp.productId).quantity).isEqualTo(StockQuantity(10))
    }

    @ParameterizedTest(name = "an order {0} is not refunded")
    @EnumSource(OrderStatus::class, names = ["AWAITING_PAYMENT", "SHIPPED", "DELIVERED", "CANCELLED", "REFUNDED"])
    fun `an order not paid, or already shipped, should not be refunded`(status: OrderStatus) {
        // Arrange
        storeOrder(status)
        // Act
        val thrown = catchThrowable { useCase.unfold(OrderRefund(orderId, status)) }
        // Assert
        assertThat(thrown).isInstanceOf(OrderNotRefundableException::class.java)
        assertThat(storedStatus()).isEqualTo(status)
        assertThat(paymentRepository.isRefunded(orderId)).isFalse()
    }

    @Test
    fun `an order whose status changed in the meantime should not be refunded`() {
        // Arrange
        storeOrder(OrderStatus.SHIPPED)
        // Act
        val thrown = catchThrowable { useCase.unfold(OrderRefund(orderId, OrderStatus.PROCESSING)) }
        // Assert
        assertThat(thrown).isInstanceOf(OrderStatusChangedException::class.java)
        assertThat(storedStatus()).isEqualTo(OrderStatus.SHIPPED)
    }

    @Test
    fun `an order whose refund is refused by the payment provider should stay as it is`() {
        // Arrange
        storeOrder(OrderStatus.PAID)
        paymentRepository.refuseRefundOf(orderId)
        // Act
        val thrown = catchThrowable { useCase.unfold(OrderRefund(orderId, OrderStatus.PAID)) }
        // Assert
        assertThat(thrown).isInstanceOf(PaymentRefundRefusedException::class.java)
        assertThat(storedStatus()).isEqualTo(OrderStatus.PAID)
        assertThat(stockRepository.stockOf(deskLamp.productId).quantity).isEqualTo(StockQuantity(8))
    }

    @Test
    fun `an unknown order should not be refunded`() {
        // Act
        val thrown = catchThrowable { useCase.unfold(OrderRefund(OrderId("unknown"), OrderStatus.PAID)) }
        // Assert
        assertThat(thrown).isInstanceOf(OrderNotFoundException::class.java)
    }
}
