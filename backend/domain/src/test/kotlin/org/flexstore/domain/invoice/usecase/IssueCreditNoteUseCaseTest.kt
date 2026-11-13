package org.flexstore.domain.invoice.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.cart.*
import org.flexstore.domain.invoice.*
import org.flexstore.domain.invoice.model.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.seller.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.UserId
import org.flexstore.infra.repository.*
import org.flexstore.domain.user.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import org.flexstore.domain.user.administration.testAdministrator
import org.flexstore.domain.user.administration.testCustomer
import org.flexstore.domain.user.administration.usersOfTheAdministration

class IssueCreditNoteUseCaseTest : WithAssertions {

    private val lampPrice = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), lampPrice)
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val paidAt = Instant.parse("2026-10-24T10:05:00Z")
    private val refundedAt = CreditNoteDate(Instant.parse("2026-10-25T09:00:00Z"))
    private val orderRepository = InMemoryOrderRepository()
    private val invoiceRepository = InMemoryInvoiceRepository()
    private val creditNoteRepository = InMemoryCreditNoteRepository()
    private val administrator = testAdministrator.id
    private val userRepository = usersOfTheAdministration()
    private val useCase =
        IssueCreditNoteUseCase(orderRepository, invoiceRepository, creditNoteRepository, userRepository)

    // An order with the given status, invoiced when it was paid
    private fun storeOrder(orderId: OrderId, status: OrderStatus, invoiced: Boolean = true) {
        val checkout = Checkout(customer, homeOrderAddresses, acceptedTermsOfSale)
        val orderPlacement = OrderPlacement(OrderStamp(orderId, paidAt), checkout)
        val order = Order.place(orderPlacement, Cart(customer).add(deskLamp, Quantity(1)))
        orderRepository.save(order.withStatus(status))
        if (invoiced) invoiceRepository.issue(InvoiceIssue(orderId, InvoiceDate(paidAt)), testSeller.id)
    }

    @Test
    fun `the refunded orders should get credit notes with the next numbers of their sequence`() {
        // Arrange
        storeOrder(OrderId("order-1"), OrderStatus.REFUNDED)
        storeOrder(OrderId("order-2"), OrderStatus.REFUNDED)
        useCase.unfold(CreditNoteIssuance(administrator, CreditNoteIssue(OrderId("order-1"), refundedAt)))
        // Act
        useCase.unfold(CreditNoteIssuance(administrator, CreditNoteIssue(OrderId("order-2"), refundedAt)))
        // Assert
        val creditNote = creditNoteRepository.findBy(OrderId("order-2"))
        val creditNoteIssue = CreditNoteIssue(OrderId("order-2"), refundedAt)
        assertThat(creditNote).isEqualTo(CreditNote(CreditNoteNumber("AV-2026-000002"), creditNoteIssue))
    }

    @Test
    fun `an order not refunded should get no credit note`() {
        // Arrange
        storeOrder(OrderId("order-1"), OrderStatus.PAID)
        // Act
        val thrown = catchThrowable {
            useCase.unfold(CreditNoteIssuance(administrator, CreditNoteIssue(OrderId("order-1"), refundedAt)))
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(OrderNotRefundedException::class.java)
        assertThat(creditNoteRepository.creditNoteIsMissingFor(OrderId("order-1"))).isTrue()
    }

    @Test
    fun `a refunded order without invoice should get no credit note`() {
        // Arrange
        storeOrder(OrderId("order-1"), OrderStatus.REFUNDED, invoiced = false)
        // Act
        val thrown = catchThrowable {
            useCase.unfold(CreditNoteIssuance(administrator, CreditNoteIssue(OrderId("order-1"), refundedAt)))
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(InvoiceNotFoundException::class.java)
    }

    @Test
    fun `a refunded order should get one credit note`() {
        // Arrange
        storeOrder(OrderId("order-1"), OrderStatus.REFUNDED)
        useCase.unfold(CreditNoteIssuance(administrator, CreditNoteIssue(OrderId("order-1"), refundedAt)))
        // Act
        val thrown = catchThrowable {
            useCase.unfold(CreditNoteIssuance(administrator, CreditNoteIssue(OrderId("order-1"), refundedAt)))
        }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(CreditNoteAlreadyIssuedException::class.java)
    }

    @Test
    fun `a customer should not issue a credit note`() {
        // Arrange
        val creditNoteIssue = CreditNoteIssue(OrderId("order-1"), refundedAt)
        // Act
        val thrown = catchThrowable { useCase.unfold(CreditNoteIssuance(testCustomer.id, creditNoteIssue)) }
        // Assert
        assertThat(thrown).hasCauseInstanceOf(CreditNoteForbiddenException::class.java)
    }
}
