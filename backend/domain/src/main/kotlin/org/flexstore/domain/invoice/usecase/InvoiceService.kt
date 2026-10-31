package org.flexstore.domain.invoice.usecase

import org.flexstore.domain.invoice.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.usecase.ReadAdministeredOrderUseCase
import org.flexstore.domain.seller.usecase.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.model.UserId

// The use cases that give the document of an invoice
data class InvoiceUseCases(
    val readInvoiceUseCase: ReadInvoiceUseCase,
    val readSellerUseCase: ReadSellerUseCase,
    val renderInvoiceUseCase: RenderInvoiceUseCase
)

// Domain service: the invoice of a paid order is downloaded by its customer, or by an administrator. The
// order is read with its customer, then its invoice and the seller it was issued with; the document is
// rendered, all through the use cases
class InvoiceService(
    private val invoiceUseCases: InvoiceUseCases,
    private val readAdministeredOrderUseCase: ReadAdministeredOrderUseCase
) {
    // The invoice of an order of another customer is reported as not found, as the order itself
    fun customerInvoice(customer: UserId.ValidUserId, orderId: OrderId): InvoicePdf {
        val administeredOrder = readAdministeredOrderUseCase.unfold(orderId)
        if (administeredOrder.order.doesNotBelongTo(customer)) {
            throw OrderNotFoundException(ErrorMessage("Order ${orderId.value} not found."))
        }
        return invoiceOf(administeredOrder)
    }

    fun anyInvoice(orderId: OrderId): InvoicePdf = invoiceOf(readAdministeredOrderUseCase.unfold(orderId))

    private fun invoiceOf(administeredOrder: AdministeredOrder): InvoicePdf {
        val invoice = invoiceUseCases.readInvoiceUseCase.unfold(administeredOrder.order.orderId)
        val seller = invoiceUseCases.readSellerUseCase.unfold(invoice.sellerId)
        val invoiceDocument = InvoiceDocument(invoice, InvoicedSale(administeredOrder, seller))
        return invoiceUseCases.renderInvoiceUseCase.unfold(invoiceDocument)
    }
}
