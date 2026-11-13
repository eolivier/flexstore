package org.flexstore.domain.invoice

import org.flexstore.domain.actor.SystemActor
import org.flexstore.domain.invoice.model.*
import org.flexstore.domain.seller.usecase.SellerVersionReading
import org.flexstore.domain.invoice.model.BillingDocument
import org.flexstore.domain.invoice.model.CreditNoteDocument
import org.flexstore.domain.invoice.model.InvoiceDocument
import org.flexstore.domain.invoice.model.InvoicePdf
import org.flexstore.domain.invoice.model.InvoicedSale
import org.flexstore.domain.invoice.usecase.*
import org.flexstore.domain.invoice.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.*
import org.flexstore.domain.order.usecase.AdministeredOrderReading
import org.flexstore.domain.order.usecase.ReadAdministeredOrderUseCase
import org.flexstore.domain.seller.usecase.*
import org.ucop.domain.entity.Actor

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
    private val readAdministeredOrderUseCase: ReadAdministeredOrderUseCase
) {
    // The invoice of an order of another customer is reported as not found, as the order itself
    fun invoice(reader: Actor, orderId: OrderId): InvoicePdf =
        render(invoiceDocumentOf(orderOf(reader, orderId)))

    // The credit note of an order of another customer is reported as not found, as the order itself
    fun creditNote(reader: Actor, orderId: OrderId): InvoicePdf =
        render(creditNoteDocumentOf(orderOf(reader, orderId)))

    // Any order for an administrator, their own ones for a customer
    private fun orderOf(reader: Actor, orderId: OrderId) =
        readAdministeredOrderUseCase.unfold(AdministeredOrderReading(reader, orderId))

    private fun invoiceDocumentOf(administeredOrder: AdministeredOrder): InvoiceDocument {
        val invoice = invoiceUseCases.readInvoiceUseCase.unfold(administeredOrder.order.orderId)
        val sellerReading = SellerVersionReading(SystemActor(), invoice.sellerId)
        val seller = invoiceUseCases.readSellerUseCase.unfold(sellerReading)
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
