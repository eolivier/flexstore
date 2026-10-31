package org.flexstore.domain.invoice

import org.flexstore.domain.order.OrderId

// The invoices issued, numbered in a sequence without gaps
interface InvoiceRepository {

    // Issues the invoice of the order, with the next number of the sequence, sold by the given seller
    fun issue(invoiceIssue: InvoiceIssue, sellerId: SellerId): Invoice

    fun findBy(orderId: OrderId): Invoice?

    fun invoiceExistsFor(orderId: OrderId) = findBy(orderId) != null

    fun invoiceIsMissingFor(orderId: OrderId) = !invoiceExistsFor(orderId)
}

// Turns the content of an invoice into a document (a PDF): the content and its wording are given by the domain
interface InvoiceRenderer {
    fun render(invoiceDocument: InvoiceDocument): InvoicePdf
}

// The shop that sells, as written on its invoices: its versions are kept, the last one saved is the current one
interface SellerRepository {

    fun current(): Seller

    fun findBy(sellerId: SellerId): Seller?

    // Adds a version of the seller, which becomes the current one
    fun save(seller: Seller)

    fun sellerIsMissing(sellerId: SellerId) = findBy(sellerId) == null
}
