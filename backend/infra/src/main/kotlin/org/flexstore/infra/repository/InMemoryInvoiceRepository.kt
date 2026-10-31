package org.flexstore.infra.repository

import org.flexstore.domain.invoice.*
import org.flexstore.domain.order.OrderId
import java.time.ZoneId

// The invoices in memory, numbered from 1 in the order they are issued: for the tests
class InMemoryInvoiceRepository : InvoiceRepository {

    private val invoices = mutableListOf<Invoice>()

    override fun issue(invoiceIssue: InvoiceIssue, sellerId: SellerId): Invoice {
        val year = invoiceIssue.issuedAt.value.atZone(ZoneId.of("Europe/Paris")).year
        val number = InvoiceNumber("$year-${(invoices.size + 1).toString().padStart(6, '0')}")
        val invoice = Invoice(number, invoiceIssue, sellerId)
        invoices.add(invoice)
        return invoice
    }

    override fun findBy(orderId: OrderId): Invoice? = invoices.firstOrNull { it.orderId == orderId }
}
