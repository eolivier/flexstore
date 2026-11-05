package org.flexstore.domain.invoice

import org.flexstore.domain.invoice.model.*
import org.flexstore.domain.invoice.model.BillingDocument
import org.flexstore.domain.invoice.model.CreditNoteDate
import org.flexstore.domain.invoice.model.CreditNoteIssue
import org.flexstore.domain.invoice.model.CreditNoteNotFoundException
import org.flexstore.domain.invoice.model.InvoiceDate
import org.flexstore.domain.invoice.model.InvoiceIssue
import org.flexstore.domain.invoice.model.InvoiceNotFoundException
import org.flexstore.domain.invoice.model.InvoicePdf
import org.flexstore.domain.invoice.usecase.*
import org.assertj.core.api.WithAssertions
import org.flexstore.domain.cart.*
import org.flexstore.domain.invoice.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.*
import org.flexstore.domain.order.usecase.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.seller.*
import org.flexstore.domain.seller.usecase.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.flexstore.infra.repository.*
import org.flexstore.domain.user.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant
import org.flexstore.domain.user.administration.testAdministrator
import org.flexstore.domain.user.administration.usersOfTheAdministration

class InvoiceServiceTest : WithAssertions {

    private val lampPrice = Price(Amount(BigDecimal(35)), Currency.EUR)
    private val deskLamp = DefinedProduct(ProductId(Identity("desk-lamp")), Name("Desk lamp"), lampPrice)
    private val janeId = UserId.ValidUserId("user-1")
    private val janeDoe = FullName(FirstName("Jane"), LastName("Doe"))
    private val jane = User.DefinedUser(janeId, janeDoe, Email("jane@example.com"), PlainPassword("password"))
    private val customer = CartOwner.Customer(janeId)
    private val orderId = OrderId("order-1")
    private val placedAt = Instant.parse("2026-10-24T10:00:00Z")
    private val orderRepository = InMemoryOrderRepository()
    private val invoiceRepository = InMemoryInvoiceRepository()
    private val creditNoteRepository = InMemoryCreditNoteRepository()
    private val refundedAt = Instant.parse("2026-10-25T09:00:00Z")
    private val userRepository = usersOfTheAdministration().apply { save(jane) }
    private val administrator = testAdministrator.id
    private val sellerRepository = InMemorySellerRepository(testSeller)
    // Renders the title of the document and the name of its seller: enough to know what was rendered
    private val titleRenderer = object : InvoiceRenderer {
        override fun render(billingDocument: BillingDocument): InvoicePdf {
            val sellerName = billingDocument.seller().asList().first().value
            return InvoicePdf("${billingDocument.title().value} - $sellerName".toByteArray())
        }
    }
    private val service = InvoiceService(
        InvoiceUseCases(
            ReadInvoiceUseCase(invoiceRepository),
            ReadCreditNoteUseCase(creditNoteRepository),
            ReadSellerUseCase(sellerRepository),
            RenderInvoiceUseCase(titleRenderer)
        ),
        ReadAdministeredOrderUseCase(orderRepository, userRepository)
    )

    private fun storePaidOrder(invoiced: Boolean = true) {
        val checkout = Checkout(customer, homeOrderAddresses, acceptedTermsOfSale)
        val orderPlacement = OrderPlacement(OrderStamp(orderId, placedAt), checkout)
        val order = Order.place(orderPlacement, Cart(customer).add(deskLamp, Quantity(1)))
        orderRepository.save(order.withStatus(OrderStatus.PAID))
        if (invoiced) invoiceRepository.issue(InvoiceIssue(orderId, InvoiceDate(placedAt)), testSeller.id)
    }

    // Paid and invoiced, then refunded with a credit note
    private fun storeRefundedOrder() {
        storePaidOrder()
        val paidOrder = orderRepository.findById(orderId)!!
        orderRepository.save(paidOrder.refundedAt(StatusChangedAt(refundedAt)))
        creditNoteRepository.issue(CreditNoteIssue(orderId, CreditNoteDate(refundedAt)))
    }

    @Test
    fun `a customer should download the invoice of their order`() {
        // Arrange
        storePaidOrder()
        // Act
        val invoicePdf = service.invoice(janeId, orderId)
        // Assert
        assertThat(String(invoicePdf.content)).isEqualTo("Facture n° 2026-000001 - Flexstore SAS")
    }

    @Test
    fun `an invoice should keep the seller it was issued with, once the seller is edited`() {
        // Arrange
        storePaidOrder()
        val renamedContact = testSellerDetail.contact.copy(name = SellerName("Flexstore Group"))
        val renamedDetail = testSellerDetail.copy(contact = renamedContact)
        val renamedSeller = Seller(SellerId.nextId(), renamedDetail)
        sellerRepository.save(renamedSeller)
        // Act
        val invoicePdf = service.invoice(administrator, orderId)
        // Assert
        assertThat(String(invoicePdf.content)).isEqualTo("Facture n° 2026-000001 - Flexstore SAS")
    }

    @Test
    fun `the invoice of the order of another customer should not be found`() {
        // Arrange
        storePaidOrder()
        // Act
        val thrown = catchThrowable { service.invoice(UserId.ValidUserId("user-2"), orderId) }
        // Assert
        assertThat(thrown).isInstanceOf(OrderNotFoundException::class.java)
    }

    @Test
    fun `an order without invoice should have no invoice to download`() {
        // Arrange
        storePaidOrder(invoiced = false)
        // Act
        val thrown = catchThrowable { service.invoice(administrator, orderId) }
        // Assert
        assertThat(thrown).isInstanceOf(InvoiceNotFoundException::class.java)
    }

    @Test
    fun `a customer should download the credit note of their refunded order`() {
        // Arrange
        storeRefundedOrder()
        // Act
        val creditNotePdf = service.creditNote(janeId, orderId)
        // Assert
        assertThat(String(creditNotePdf.content)).isEqualTo("Avoir n° AV-2026-000001 - Flexstore SAS")
    }

    @Test
    fun `the credit note of the order of another customer should not be found`() {
        // Arrange
        storeRefundedOrder()
        // Act
        val thrown = catchThrowable { service.creditNote(UserId.ValidUserId("user-2"), orderId) }
        // Assert
        assertThat(thrown).isInstanceOf(OrderNotFoundException::class.java)
    }

    @Test
    fun `a paid order that is not refunded should have no credit note to download`() {
        // Arrange
        storePaidOrder()
        // Act
        val thrown = catchThrowable { service.creditNote(administrator, orderId) }
        // Assert
        assertThat(thrown).isInstanceOf(CreditNoteNotFoundException::class.java)
    }

    @Test
    fun `an administrator should download the invoice of the order of any customer`() {
        // Arrange
        storePaidOrder()
        // Act
        val invoicePdf = service.invoice(administrator, orderId)
        // Assert
        assertThat(String(invoicePdf.content)).isEqualTo("Facture n° 2026-000001 - Flexstore SAS")
    }
}
