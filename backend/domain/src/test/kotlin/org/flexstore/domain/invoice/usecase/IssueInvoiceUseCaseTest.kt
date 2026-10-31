package org.flexstore.domain.invoice.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.cart.*
import org.flexstore.domain.invoice.*
import org.flexstore.domain.order.*
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.UserId
import org.flexstore.infra.repository.InMemoryInvoiceRepository
import org.flexstore.infra.repository.InMemoryOrderRepository
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

class IssueInvoiceUseCaseTest : WithAssertions {

    private val lampPrice = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), lampPrice)
    private val customer = CartOwner.Customer(UserId.ValidUserId("user-1"))
    private val issuedAt = InvoiceDate(Instant.parse("2026-10-24T10:05:00Z"))
    private val orderRepository = InMemoryOrderRepository()
    private val invoiceRepository = InMemoryInvoiceRepository()
    private val useCase = IssueInvoiceUseCase(orderRepository, invoiceRepository)

    private fun storeOrder(orderId: OrderId, status: OrderStatus) {
        val checkout = Checkout(customer, homeOrderAddresses)
        val orderPlacement = OrderPlacement(OrderStamp(orderId, issuedAt.value), checkout)
        val order = Order.place(orderPlacement, Cart(customer).add(deskLamp, Quantity(1)))
        orderRepository.save(order.withStatus(status))
    }

    @Test
    fun `the paid orders should be invoiced with the next numbers of the sequence`() {
        // Arrange
        storeOrder(OrderId("order-1"), OrderStatus.PAID)
        storeOrder(OrderId("order-2"), OrderStatus.PAID)
        useCase.unfold(InvoiceIssue(OrderId("order-1"), issuedAt))
        // Act
        useCase.unfold(InvoiceIssue(OrderId("order-2"), issuedAt))
        // Assert
        val invoice = invoiceRepository.findBy(OrderId("order-2"))
        assertThat(invoice).isEqualTo(Invoice(InvoiceNumber("2026-000002"), OrderId("order-2"), issuedAt))
    }

    @Test
    fun `an order not paid should not be invoiced`() {
        // Arrange
        storeOrder(OrderId("order-1"), OrderStatus.AWAITING_PAYMENT)
        // Act
        val thrown = catchThrowable { useCase.unfold(InvoiceIssue(OrderId("order-1"), issuedAt)) }
        // Assert
        assertThat(thrown).isInstanceOf(OrderNotPaidException::class.java)
        assertThat(invoiceRepository.invoiceIsMissingFor(OrderId("order-1"))).isTrue()
    }

    @Test
    fun `an order should be invoiced once`() {
        // Arrange
        storeOrder(OrderId("order-1"), OrderStatus.PAID)
        useCase.unfold(InvoiceIssue(OrderId("order-1"), issuedAt))
        // Act
        val thrown = catchThrowable { useCase.unfold(InvoiceIssue(OrderId("order-1"), issuedAt)) }
        // Assert
        assertThat(thrown).isInstanceOf(InvoiceAlreadyIssuedException::class.java)
    }
}
