package org.flexstore.domain.invoice

import org.flexstore.domain.order.OrderId

// The invoices issued, numbered in a sequence without gaps
interface InvoiceRepository {

    // Issues the invoice of the order, with the next number of the sequence
    fun issue(invoiceIssue: InvoiceIssue): Invoice

    fun findBy(orderId: OrderId): Invoice?

    fun invoiceExistsFor(orderId: OrderId) = findBy(orderId) != null

    fun invoiceIsMissingFor(orderId: OrderId) = !invoiceExistsFor(orderId)
}

// Turns the content of an invoice into a document (a PDF): the content and its wording are given by the domain
interface InvoiceRenderer {
    fun render(invoiceDocument: InvoiceDocument): InvoicePdf
}

// The shop that sells, as written on its invoices (given by the configuration)
interface SellerRepository {
    fun load(): Seller
}
