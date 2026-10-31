package org.flexstore.domain.invoice.usecase

import org.flexstore.domain.invoice.*
import org.flexstore.domain.order.OrderId
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*

// The invoice of an order, issued when it was paid
class ReadInvoiceUseCase(private val invoiceRepository: InvoiceRepository) : QueryUseCase<OrderId, Invoice> {

    override fun getPreConditions(): PreConditions<OrderId> = PreConditions(invoiceExistsCondition())

    override fun getNominalQuery() = NominalQuery<OrderId, Invoice> { orderId ->
        invoiceRepository.findBy(orderId)!!
    }

    override fun getPostConditions(): PostConditions<Invoice> = PostConditions.none()

    private fun invoiceExistsCondition() = PreCondition<OrderId> { orderId ->
        if (invoiceRepository.invoiceIsMissingFor(orderId)) {
            throw InvoiceNotFoundException(ErrorMessage("Order ${orderId.value} has no invoice: it is not paid."))
        }
    }
}
