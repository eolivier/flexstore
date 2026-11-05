package org.flexstore.infra.repository

import org.flexstore.domain.invoice.*
import org.flexstore.domain.invoice.model.*
import org.flexstore.domain.order.model.OrderId
import org.flexstore.domain.seller.*
import java.time.ZoneId

private const val NUMBER_DIGITS = 6

// The invoices in memory, numbered from 1 in the order they are issued: for the tests
class InMemoryInvoiceRepository : InvoiceRepository {

    private val invoices = mutableListOf<Invoice>()

    override fun issue(invoiceIssue: InvoiceIssue, sellerId: SellerId): Invoice {
        val year = invoiceIssue.issuedAt.value.atZone(ZoneId.of("Europe/Paris")).year
        val number = InvoiceNumber("$year-${(invoices.size + 1).toString().padStart(NUMBER_DIGITS, '0')}")
        val invoice = Invoice(number, invoiceIssue, sellerId)
        invoices.add(invoice)
        return invoice
    }

    override fun findBy(orderId: OrderId): Invoice? = invoices.firstOrNull { it.orderId == orderId }
}
