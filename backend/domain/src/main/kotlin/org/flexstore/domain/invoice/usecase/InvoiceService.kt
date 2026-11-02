package org.flexstore.domain.invoice.usecase

import org.flexstore.domain.invoice.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.usecase.*
import org.flexstore.domain.seller.usecase.*
import org.flexstore.domain.user.model.UserId

// The use cases that read the order of an invoice or of a credit note: by its customer, or by an administrator
data class BilledOrderUseCases(
    val readCustomerOrderWithCustomerUseCase: ReadCustomerOrderWithCustomerUseCase,
    val readAdministeredOrderUseCase: ReadAdministeredOrderUseCase
)

// The use cases that give the document of an invoice or of a credit note
data class InvoiceUseCases(
    val readInvoiceUseCase: ReadInvoiceUseCase,
    val readCreditNoteUseCase: ReadCreditNoteUseCase,
    val readSellerUseCase: ReadSellerUseCase,
    val renderInvoiceUseCase: RenderInvoiceUseCase
)

// Domain service: the invoice of a paid order, and the credit note of a refunded one, are downloaded by its
// customer, or by an administrator. The order is read with its customer, then its invoice and the seller it was
// issued with (and its credit note); the document is rendered, all through the use cases
class InvoiceService(
    private val invoiceUseCases: InvoiceUseCases,
    private val billedOrderUseCases: BilledOrderUseCases
) {
    // The invoice of an order of another customer is reported as not found, as the order itself
    fun customerInvoice(customer: UserId.ValidUserId, orderId: OrderId): InvoicePdf =
        render(invoiceDocumentOf(customerOrder(customer, orderId)))

    fun anyInvoice(reader: UserId, orderId: OrderId): InvoicePdf =
        render(invoiceDocumentOf(anyOrder(reader, orderId)))

    // The credit note of an order of another customer is reported as not found, as the order itself
    fun customerCreditNote(customer: UserId.ValidUserId, orderId: OrderId): InvoicePdf =
        render(creditNoteDocumentOf(customerOrder(customer, orderId)))

    fun anyCreditNote(reader: UserId, orderId: OrderId): InvoicePdf =
        render(creditNoteDocumentOf(anyOrder(reader, orderId)))

    private fun customerOrder(customer: UserId.ValidUserId, orderId: OrderId) =
        billedOrderUseCases.readCustomerOrderWithCustomerUseCase.unfold(CustomerOrderLookup(customer, orderId))

    private fun anyOrder(reader: UserId, orderId: OrderId) =
        billedOrderUseCases.readAdministeredOrderUseCase.unfold(AdministeredOrderReading(reader, orderId))

    private fun invoiceDocumentOf(administeredOrder: AdministeredOrder): InvoiceDocument {
        val invoice = invoiceUseCases.readInvoiceUseCase.unfold(administeredOrder.order.orderId)
        val seller = invoiceUseCases.readSellerUseCase.unfold(invoice.sellerId)
        return InvoiceDocument(invoice, InvoicedSale(administeredOrder, seller))
    }

    // The credit note is read first: an order without credit note is reported so, even when it has an invoice
    private fun creditNoteDocumentOf(administeredOrder: AdministeredOrder): CreditNoteDocument {
        val creditNote = invoiceUseCases.readCreditNoteUseCase.unfold(administeredOrder.order.orderId)
        return CreditNoteDocument(creditNote, invoiceDocumentOf(administeredOrder))
    }

    private fun render(billingDocument: BillingDocument) =
        invoiceUseCases.renderInvoiceUseCase.unfold(billingDocument)
}
