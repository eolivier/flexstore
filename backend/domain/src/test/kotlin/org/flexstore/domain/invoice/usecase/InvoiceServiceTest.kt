package org.flexstore.domain.invoice.usecase

import org.assertj.core.api.WithAssertions
import org.flexstore.domain.cart.*
import org.flexstore.domain.invoice.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.usecase.ReadAdministeredOrderUseCase
import org.flexstore.domain.product.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*
import org.flexstore.infra.invoice.ConfiguredSellerRepository
import org.flexstore.infra.repository.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.Instant

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
    private val userRepository = InMemoryUserRepository().apply { save(jane) }
    // Renders the title of the invoice only: enough to know which invoice was rendered
    private val titleRenderer = object : InvoiceRenderer {
        override fun render(invoiceDocument: InvoiceDocument) =
            InvoicePdf(invoiceDocument.title().value.toByteArray())
    }
    private val service = InvoiceService(
        InvoiceUseCases(
            ReadInvoiceUseCase(invoiceRepository),
            ReadSellerUseCase(ConfiguredSellerRepository(testSeller)),
            RenderInvoiceUseCase(titleRenderer)
        ),
        ReadAdministeredOrderUseCase(orderRepository, userRepository)
    )

    private fun storePaidOrder(invoiced: Boolean = true) {
        val checkout = Checkout(customer, homeOrderAddresses)
        val orderPlacement = OrderPlacement(OrderStamp(orderId, placedAt), checkout)
        val order = Order.place(orderPlacement, Cart(customer).add(deskLamp, Quantity(1)))
        orderRepository.save(order.withStatus(OrderStatus.PAID))
        if (invoiced) invoiceRepository.issue(InvoiceIssue(orderId, InvoiceDate(placedAt)))
    }

    @Test
    fun `a customer should download the invoice of their order`() {
        // Arrange
        storePaidOrder()
        // Act
        val invoicePdf = service.customerInvoice(janeId, orderId)
        // Assert
        assertThat(String(invoicePdf.content)).isEqualTo("Facture n° 2026-000001")
    }

    @Test
    fun `the invoice of the order of another customer should not be found`() {
        // Arrange
        storePaidOrder()
        // Act
        val thrown = catchThrowable { service.customerInvoice(UserId.ValidUserId("user-2"), orderId) }
        // Assert
        assertThat(thrown).isInstanceOf(OrderNotFoundException::class.java)
    }

    @Test
    fun `an order without invoice should have no invoice to download`() {
        // Arrange
        storePaidOrder(invoiced = false)
        // Act
        val thrown = catchThrowable { service.anyInvoice(orderId) }
        // Assert
        assertThat(thrown).isInstanceOf(InvoiceNotFoundException::class.java)
    }
}
