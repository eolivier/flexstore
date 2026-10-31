package org.flexstore.domain.payment.usecase

import org.flexstore.domain.stock.*
import org.assertj.core.api.WithAssertions
import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.usecase.*
import org.flexstore.domain.invoice.usecase.IssueInvoiceUseCase
import org.flexstore.domain.payment.*
import org.flexstore.domain.product.*
import org.flexstore.domain.settings.ReadShopSettingsUseCase
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.flexstore.infra.repository.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class PaymentServiceTest : WithAssertions {

    private val price = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), price)
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val janeDoe = FullName(FirstName("Jane"), LastName("Doe"))
    private val email = Email("jane@example.com")
    private val definedUser = User.DefinedUser(customer.userId, janeDoe, email, PlainPassword("pwd"))
    private val orderId = OrderId("order-1")
    private val orderRepository = InMemoryOrderRepository()
    private val deskLampStock = Stock(deskLamp.productId, StockQuantity(5))
    private val stockRepository = InMemoryStockRepository().apply { save(deskLampStock) }
    private val cartRepository = InMemoryCartRepository()
    private val userRepository = InMemoryUserRepository().apply { save(definedUser) }
    private val emailRepository = InMemoryEmailRepository()
    private val orderQueryUseCases = OrderQueryUseCases(
        ListCustomerOrdersUseCase(orderRepository),
        ListAllOrdersUseCase(orderRepository),
        ReadAnyOrderUseCase(orderRepository),
        ReadCustomerOrderUseCase(orderRepository)
    )
    private val shopSettingsRepository = InMemoryShopSettingsRepository()
    private val invoiceRepository = InMemoryInvoiceRepository()
    private val sendEmailUseCase =
        SendOrderConfirmationEmailUseCase(shopSettingsRepository, userRepository, emailRepository)

    private fun paymentServiceWith(paymentRepository: PaymentRepository): PaymentService {
        val startPaymentUseCase = StartPaymentUseCase(orderRepository, paymentRepository, userRepository)
        val confirmPaymentUseCase = ConfirmPaymentUseCase(orderRepository, cartRepository, stockRepository)
        val readPaymentUseCase = ReadPaymentUseCase(paymentRepository)
        val paymentUseCases = PaymentUseCases(startPaymentUseCase, confirmPaymentUseCase, readPaymentUseCase)
        val readShopSettingsUseCase = ReadShopSettingsUseCase(shopSettingsRepository)
        val orderUseCases =
            OrderUseCases(
                orderQueryUseCases,
                sendEmailUseCase,
                readShopSettingsUseCase,
                ProposeShippingOptionsUseCase(cartRepository, shopSettingsRepository)
            )
        val issueInvoiceUseCase = IssueInvoiceUseCase(orderRepository, invoiceRepository)
        return PaymentService(paymentUseCases, orderUseCases, issueInvoiceUseCase)
    }

    private val paidAtOnce = paymentServiceWith(InMemoryPaymentRepository(PaymentOutcome.PAID))
    private val pendingPaymentRepository = InMemoryPaymentRepository(PaymentOutcome.PENDING)
    private val paidLater = paymentServiceWith(pendingPaymentRepository)

    private fun placeOrder() {
        val cart = Cart(customer).add(deskLamp, Quantity(1))
        cartRepository.save(cart)
        val checkout = Checkout(customer, homeOrderAddresses)
        val orderPlacement = OrderPlacement(OrderStamp(orderId, Instant.now()), checkout)
        orderRepository.save(Order.place(orderPlacement, cart))
    }

    private fun storedStatus() = orderRepository.findById(orderId)?.status

    @Test
    fun `a payment confirmed later should leave the order awaiting it`() {
        // Arrange
        placeOrder()
        // Act
        val paymentSession = paidLater.pay(OrderPayment(customer, orderId))
        // Assert
        assertThat(paymentSession.checkoutPage).isEqualTo(CheckoutPage("https://pay.example/order-1"))
        assertThat(storedStatus()).isEqualTo(OrderStatus.AWAITING_PAYMENT)
        assertThat(cartRepository.load(customer).hasItems()).isTrue()
    }

    @Test
    fun `the payment page should be prefilled with the email and the name of the customer`() {
        // Arrange
        placeOrder()
        // Act
        paidLater.pay(OrderPayment(customer, orderId))
        // Assert
        assertThat(pendingPaymentRepository.payerOf(orderId)).isEqualTo(Payer(email, Cardholder.Named(janeDoe)))
    }

    @Test
    fun `a confirmed payment should pay the order, empty the cart and confirm the order by email`() {
        // Arrange
        placeOrder()
        paidLater.pay(OrderPayment(customer, orderId))
        // Act
        paidLater.confirm(orderId)
        // Assert
        assertThat(storedStatus()).isEqualTo(OrderStatus.PAID)
        assertThat(cartRepository.load(customer).isEmpty()).isTrue()
        assertThat(emailRepository.sentEmails()).hasSize(1)
    }

    @Test
    fun `a confirmed payment should invoice the order`() {
        // Arrange
        placeOrder()
        paidLater.pay(OrderPayment(customer, orderId))
        // Act
        paidLater.confirm(orderId)
        // Assert
        assertThat(invoiceRepository.invoiceExistsFor(orderId)).isTrue()
    }

    @Test
    fun `a confirmed payment should not take the units reserved by the order out a second time`() {
        // Arrange
        placeOrder()
        // Act
        paidAtOnce.pay(OrderPayment(customer, orderId))
        // Assert
        assertThat(stockRepository.stockOf(deskLamp.productId).quantity).isEqualTo(StockQuantity(5))
    }

    @Test
    fun `an order cancelled while its customer was paying should be paid, and take its units out again`() {
        // Arrange
        placeOrder()
        orderRepository.save(orderRepository.findById(orderId)!!.withStatus(OrderStatus.CANCELLED))
        // Act
        paidLater.confirm(orderId)
        // Assert
        assertThat(storedStatus()).isEqualTo(OrderStatus.PAID)
        assertThat(stockRepository.stockOf(deskLamp.productId).quantity).isEqualTo(StockQuantity(4))
    }

    @Test
    fun `a payment confirmed at once should pay the order`() {
        // Arrange
        placeOrder()
        // Act
        paidAtOnce.pay(OrderPayment(customer, orderId))
        // Assert
        assertThat(storedStatus()).isEqualTo(OrderStatus.PAID)
    }

    @Test
    fun `a payment notified twice should confirm the order once`() {
        // Arrange
        placeOrder()
        paidLater.pay(OrderPayment(customer, orderId))
        paidLater.confirm(orderId)
        // Act
        paidLater.confirm(orderId)
        // Assert
        assertThat(emailRepository.sentEmails()).hasSize(1)
    }

    @Test
    fun `a paid order should not be paid again`() {
        // Arrange
        placeOrder()
        paidAtOnce.pay(OrderPayment(customer, orderId))
        // Act
        val thrown = catchThrowable { paidAtOnce.pay(OrderPayment(customer, orderId)) }
        // Assert
        assertThat(thrown).isInstanceOf(OrderAlreadyPaidException::class.java)
    }

    @Test
    fun `the order of another customer should not be paid`() {
        // Arrange
        placeOrder()
        val otherCustomer = CartOwner.Customer(UserId.ValidUserId("user-2"))
        // Act
        val thrown = catchThrowable { paidAtOnce.pay(OrderPayment(otherCustomer, orderId)) }
        // Assert
        assertThat(thrown).isInstanceOf(OrderNotFoundException::class.java)
    }
}
