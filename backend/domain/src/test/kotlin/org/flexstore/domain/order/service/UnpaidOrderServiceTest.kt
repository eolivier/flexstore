package org.flexstore.domain.order.service

import org.flexstore.domain.order.model.*
import org.flexstore.domain.order.usecase.*
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.WithAssertions
import org.flexstore.domain.actor.*
import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.payment.PaymentOutcome
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.settings.*
import org.flexstore.domain.settings.usecase.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.stock.*
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.systemUser
import org.ucop.domain.ErrorMessage
import org.ucop.domain.NominalException
import org.ucop.domain.PreConditionException
import org.ucop.domain.entity.Actor
import org.flexstore.infra.repository.*
import org.junit.jupiter.api.AfterEach
import org.flexstore.domain.user.model.AccountStatus
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

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

    private val service = UnpaidOrderService(
        ActorResolutionService(InMemoryUserRepository().apply { save(systemUser) }),
        ReadShopSettingsUseCase(shopSettingsRepository),
        UnpaidOrderUseCases(ListAllOrdersUseCase(orderRepository), cancelUnpaidOrderUseCase)
    )

    // The late orders are looked for at the given moment
    private fun cancelLateOrdersAt(instant: Instant): Orders {
        InstantProvider.fixAt(instant)
        return service.cancelLateOrders()
    }

    @AfterEach
    fun resetTheMoment() {
        InstantProvider.reset()
    }

    private fun placeOrder() {
        val cart = Cart(customer).add(deskLamp, Quantity(2))
        val checkout = Checkout(customer, homeOrderAddresses, acceptedTermsOfSale)
        val orderPlacement = OrderPlacement(OrderStamp(orderId, placedAt), checkout)
        orderRepository.save(Order.place(orderPlacement, cart))
    }

    private fun storedStatus() = orderRepository.findById(orderId)?.status

    @Test
    fun `an order not paid within the payment delay should be cancelled, and its payment stopped`() {
        // Arrange
        placeOrder()
        // Act
        cancelLateOrdersAt(placedAt.plusSeconds(10 * 60))
        // Assert
        assertThat(storedStatus()).isEqualTo(OrderStatus.CANCELLED)
        assertThat(paymentRepository.isStopped(orderId)).isTrue()
    }

    @Test
    fun `a cancelled order should give its units back to the stock`() {
        // Arrange
        placeOrder()
        // Act
        cancelLateOrdersAt(placedAt.plusSeconds(10 * 60))
        // Assert
        assertThat(stockRepository.stockOf(deskLamp.productId).quantity).isEqualTo(StockQuantity(10))
    }

    @Test
    fun `an order within the payment delay should still await its payment`() {
        // Arrange
        placeOrder()
        // Act
        cancelLateOrdersAt(placedAt.plusSeconds(10 * 60 - 1))
        // Assert
        assertThat(storedStatus()).isEqualTo(OrderStatus.AWAITING_PAYMENT)
    }

    @Test
    fun `an order whose customer has just paid should not be cancelled`() {
        // Arrange
        placeOrder()
        paymentRepository.completePaymentOf(orderId)
        // Act
        val cancelledOrders = cancelLateOrdersAt(placedAt.plusSeconds(10 * 60))
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
        cancelLateOrdersAt(placedAt.plusSeconds(3600))
        // Assert
        assertThat(storedStatus()).isEqualTo(OrderStatus.PAID)
    }

    private fun cancellationBy(actor: Actor): UnpaidOrderCancellation {
        val timeLimit = PaymentTimeLimit(PaymentDelay(10), placedAt.plusSeconds(10 * 60))
        return UnpaidOrderCancellation(actor, orderId, timeLimit)
    }

    @Test
    fun `an administrator should cancel an order not paid in time`() {
        // Arrange
        placeOrder()
        // Act
        cancelUnpaidOrderUseCase.unfold(
            cancellationBy(Admin(customer.userId, Name("Jane Doe"), AccountStatus.ACTIVE))
        )
        // Assert
        assertThat(storedStatus()).isEqualTo(OrderStatus.CANCELLED)
    }

    @Test
    fun `a customer should not cancel an order not paid in time`() {
        // Arrange
        placeOrder()
        // Act
        val thrown = catchThrowable {
            cancelUnpaidOrderUseCase.unfold(
                cancellationBy(Customer(customer.userId, Name("Jane Doe"), AccountStatus.ACTIVE))
            )
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UnpaidOrderForbiddenException::class.java)
        assertThat(storedStatus()).isEqualTo(OrderStatus.AWAITING_PAYMENT)
    }

    @Test
    fun `a visitor should not cancel an order not paid in time`() {
        // Arrange
        placeOrder()
        // Act
        val thrown = catchThrowable { cancelUnpaidOrderUseCase.unfold(cancellationBy(Visitor())) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(UnpaidOrderForbiddenException::class.java)
        assertThat(paymentRepository.isStopped(orderId)).isFalse()
    }

    private class OrderNotCancelledException(errorMessage: ErrorMessage) : PreConditionException(errorMessage)

    // A service whose use case fails with the given exception, caused by the given precondition
    private fun serviceFailingWith(
        precondition: PreConditionException
    ): Pair<UnpaidOrderService, NominalException> {
        val failure = NominalException(precondition)
        val failingUseCase = mockk<CancelUnpaidOrderUseCase>()
        every { failingUseCase.unfold(any()) } throws failure
        val failingService = UnpaidOrderService(
            ActorResolutionService(InMemoryUserRepository().apply { save(systemUser) }),
            ReadShopSettingsUseCase(shopSettingsRepository),
            UnpaidOrderUseCases(ListAllOrdersUseCase(orderRepository), failingUseCase)
        )
        return Pair(failingService, failure)
    }

    @Test
    fun `the refusal of the system to cancel an order should stop the run`() {
        // Arrange
        placeOrder()
        val refusal = UnpaidOrderForbiddenException(ErrorMessage("Only an administrator or the system."))
        val (refusingService, failure) = serviceFailingWith(refusal)
        InstantProvider.fixAt(placedAt.plusSeconds(10 * 60))
        // Act
        val thrown = catchThrowable { refusingService.cancelLateOrders() }
        // Assert
        assertThat(thrown).isSameAs(failure)
    }

    @Test
    fun `another precondition not met should only set the order aside`() {
        // Arrange
        placeOrder()
        val (failingService, _) = serviceFailingWith(OrderNotCancelledException(ErrorMessage("Cannot cancel.")))
        InstantProvider.fixAt(placedAt.plusSeconds(10 * 60))
        // Act
        val cancelledOrders = failingService.cancelLateOrders()
        // Assert
        assertThat(cancelledOrders.asList()).isEmpty()
    }
}
