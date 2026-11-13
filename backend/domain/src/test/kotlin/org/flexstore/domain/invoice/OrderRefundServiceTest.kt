package org.flexstore.domain.invoice

import org.flexstore.domain.invoice.model.*
import org.flexstore.domain.actor.testCustomerActor
import org.flexstore.domain.actor.testAdministratorActor
import org.flexstore.domain.invoice.model.CreditNote
import org.flexstore.domain.invoice.model.CreditNoteDate
import org.flexstore.domain.invoice.model.CreditNoteIssue
import org.flexstore.domain.invoice.model.CreditNoteNumber
import org.flexstore.domain.invoice.model.InvoiceDate
import org.flexstore.domain.invoice.model.InvoiceIssue
import org.flexstore.domain.invoice.usecase.*
import org.assertj.core.api.WithAssertions
import org.flexstore.domain.cart.*
import org.flexstore.domain.invoice.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.*
import org.flexstore.domain.order.usecase.*
import org.flexstore.domain.payment.PaymentOutcome
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.seller.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.stock.*
import org.flexstore.domain.user.model.*
import org.flexstore.infra.repository.*
import org.flexstore.domain.user.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import org.flexstore.domain.user.administration.usersOfTheAdministration

class OrderRefundServiceTest : WithAssertions {

    private val lampPrice = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val deskLamp = DefinedProduct(ProductId("desk-lamp"), Name("Desk lamp"), lampPrice)
    private val janeId = UserId.ValidUserId("user-1")
    private val janeDoe = FullName(FirstName("Jane"), LastName("Doe"))
    private val jane = User.DefinedUser(janeId, janeDoe, Email("jane@example.com"), PlainPassword("password"))
    private val customer = CartOwner.Customer(janeId)
    private val orderId = OrderId("order-1")
    private val paidAt = Instant.parse("2026-10-24T10:05:00Z")
    private val refundedAt = Instant.parse("2026-10-25T09:00:00Z")
    private val orderRepository = InMemoryOrderRepository()
    private val stockRepository = InMemoryStockRepository().apply {
        save(Stock(deskLamp.productId, StockQuantity(8)))
    }
    private val paymentRepository = InMemoryPaymentRepository(PaymentOutcome.PAID)
    private val invoiceRepository = InMemoryInvoiceRepository()
    private val creditNoteRepository = InMemoryCreditNoteRepository()
    private val userRepository = usersOfTheAdministration().apply { save(jane) }
    private val administrator = testAdministratorActor
    private val emailRepository = InMemoryEmailRepository()
    private val service = OrderRefundService(
        RefundOrderUseCase(orderRepository, stockRepository, paymentRepository),
        IssueCreditNoteUseCase(orderRepository, invoiceRepository, creditNoteRepository),
        RefundedOrderUseCases(
            ReadAdministeredOrderUseCase(orderRepository, userRepository),
            SendOrderStatusEmailUseCase(userRepository, emailRepository)
        )
    )

    // The orders are refunded at a known moment
    @BeforeEach
    fun fixTheMoment() {
        InstantProvider.fixAt(refundedAt)
    }

    @AfterEach
    fun resetTheMoment() {
        InstantProvider.reset()
    }

    private fun storePaidOrder() {
        val checkout = Checkout(customer, homeOrderAddresses, acceptedTermsOfSale)
        val orderPlacement = OrderPlacement(OrderStamp(orderId, paidAt), checkout)
        val order = Order.place(orderPlacement, Cart(customer).add(deskLamp, Quantity(2)))
        orderRepository.save(order.withStatus(OrderStatus.PAID))
        invoiceRepository.issue(InvoiceIssue(orderId, InvoiceDate(paidAt)), testSeller.id)
    }

    @Test
    fun `a refunded order should get the credit note of its invoice, at the moment it is refunded`() {
        // Arrange
        storePaidOrder()
        // Act
        val refundedOrder = service.refund(OrderRefund(administrator, orderId, OrderStatus.PAID))
        // Assert
        assertThat(refundedOrder.order.isRefunded()).isTrue()
        val creditNoteIssue = CreditNoteIssue(orderId, CreditNoteDate(refundedAt))
        val creditNote = CreditNote(CreditNoteNumber("AV-2026-000001"), creditNoteIssue)
        assertThat(creditNoteRepository.findBy(orderId)).isEqualTo(creditNote)
    }

    @Test
    fun `the customer of a refunded order should be told by email`() {
        // Arrange
        storePaidOrder()
        // Act
        service.refund(OrderRefund(administrator, orderId, OrderStatus.PAID))
        // Assert
        assertThat(emailRepository.sentOrderStatusEmails()).hasSize(1)
    }

    @Test
    fun `an order whose status changed in the meantime should get no credit note`() {
        // Arrange
        storePaidOrder()
        val orderRefund = OrderRefund(administrator, orderId, OrderStatus.PROCESSING)
        // Act
        val thrown = catchThrowable { service.refund(orderRefund) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(OrderStatusChangedException::class.java)
        assertThat(creditNoteRepository.creditNoteIsMissingFor(orderId)).isTrue()
    }

    @Test
    fun `a customer should not refund an order`() {
        // Arrange
        storePaidOrder()
        // Act
        val thrown = catchThrowable { service.refund(OrderRefund(testCustomerActor, orderId, OrderStatus.PAID)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(OrderAdministrationForbiddenException::class.java)
        assertThat(orderRepository.findById(orderId)?.status).isEqualTo(OrderStatus.PAID)
    }
}
