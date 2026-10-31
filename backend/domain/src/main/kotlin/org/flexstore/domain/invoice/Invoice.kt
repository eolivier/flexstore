package org.flexstore.domain.invoice

import org.flexstore.domain.order.OrderId
import org.flexstore.domain.seller.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.NominalException
import org.flexstore.domain.sharedkernel.InstantProvider
import java.time.Instant

// The number of an invoice, in a sequence without gaps: the year of issue, then its rank (2026-000042)
data class InvoiceNumber(val value: String)

// The moment an invoice is issued: when the payment of its order is confirmed
data class InvoiceDate(val value: Instant) {
    companion object {
        fun now() = InvoiceDate(InstantProvider.now())
    }
}

// An invoice to issue for a paid order, at a given moment
data class InvoiceIssue(val orderId: OrderId, val issuedAt: InvoiceDate)

// The invoice of a paid order, numbered when it is issued, with the version of the seller current at that time
data class Invoice(val number: InvoiceNumber, val issue: InvoiceIssue, val sellerId: SellerId) {

    val orderId get() = issue.orderId

    val issuedAt get() = issue.issuedAt
}

// The invoice as a PDF document, to download
data class InvoicePdf(val content: ByteArray) {
    override fun equals(other: Any?) = other is InvoicePdf && content.contentEquals(other.content)
    override fun hashCode() = content.contentHashCode()
}

data class InvoiceNotFoundException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class InvoiceAlreadyIssuedException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
data class OrderNotPaidException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class InvoiceIssueFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
