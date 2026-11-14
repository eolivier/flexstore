package org.flexstore.domain.invoice

import org.flexstore.domain.invoice.model.BillingDocument
import org.flexstore.domain.invoice.model.CreditNote
import org.flexstore.domain.invoice.model.CreditNoteIssue
import org.flexstore.domain.invoice.model.Invoice
import org.flexstore.domain.invoice.model.InvoiceIssue
import org.flexstore.domain.invoice.model.InvoicePdf
import org.flexstore.domain.order.model.OrderId
import org.flexstore.domain.seller.*
import org.flexstore.domain.invoice.model.InvoiceNotFoundException
import org.flexstore.domain.invoice.model.CreditNoteNotFoundException

// The invoices issued, numbered in a sequence without gaps
interface InvoiceRepository {

    // Issues the invoice of the order, with the next number of the sequence, sold by the given seller
    fun issue(invoiceIssue: InvoiceIssue, sellerId: SellerId): Invoice

    fun findBy(orderId: OrderId): Invoice?

    // The invoice, which a precondition has checked to exist
    fun get(orderId: OrderId): Invoice =
        findBy(orderId) ?: throw InvoiceNotFoundException("No invoice for order $orderId.")

    fun invoiceExistsFor(orderId: OrderId) = findBy(orderId) != null

    fun invoiceIsMissingFor(orderId: OrderId) = !invoiceExistsFor(orderId)
}

// The credit notes issued, numbered in their own sequence without gaps
interface CreditNoteRepository {

    // Issues the credit note of the order, which cancels its invoice, with the next number of the sequence
    fun issue(creditNoteIssue: CreditNoteIssue): CreditNote

    fun findBy(orderId: OrderId): CreditNote?

    // The credit note, which a precondition has checked to exist
    fun get(orderId: OrderId): CreditNote =
        findBy(orderId) ?: throw CreditNoteNotFoundException("No credit note for order $orderId.")

    fun creditNoteExistsFor(orderId: OrderId) = findBy(orderId) != null

    fun creditNoteIsMissingFor(orderId: OrderId) = !creditNoteExistsFor(orderId)
}

// Turns the content of an invoice or of a credit note into a document (a PDF): the content and its wording are
// given by the domain
interface InvoiceRenderer {
    fun render(billingDocument: BillingDocument): InvoicePdf
}
