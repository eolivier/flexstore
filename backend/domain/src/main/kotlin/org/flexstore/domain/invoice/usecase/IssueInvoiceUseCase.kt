package org.flexstore.domain.invoice.usecase

import org.flexstore.domain.invoice.*
import org.flexstore.domain.order.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*

// A paid order is invoiced once, with the next number of the sequence of the invoices, sold by the current
// seller
class IssueInvoiceUseCase(
    private val orderRepository: OrderRepository,
    private val invoiceRepository: InvoiceRepository,
    private val sellerRepository: SellerRepository
) : UseCase<InvoiceIssue> {

    override fun getPreConditions(): PreConditions<InvoiceIssue> =
        PreConditions(orderIsPaidCondition(), invoiceIsNotIssuedCondition())

    override fun getNominalScenario(): NominalScenario<InvoiceIssue> =
        NominalScenario(Step { invoiceIssue ->
            val currentSeller = sellerRepository.current()
            invoiceRepository.issue(invoiceIssue, currentSeller.id)
        })

    override fun getPostConditions(): PostConditions<InvoiceIssue> = PostConditions(invoiceIsIssuedCondition())

    private fun orderIsPaidCondition() = PreCondition<InvoiceIssue> { invoiceIssue ->
        val orderId = invoiceIssue.orderId
        if (orderRepository.orderIsMissing(orderId) || orderRepository.findById(orderId)!!.isNotPaid()) {
            throw OrderNotPaidException(ErrorMessage("Order ${orderId.value} is not paid: no invoice."))
        }
    }

    private fun invoiceIsNotIssuedCondition() = PreCondition<InvoiceIssue> { invoiceIssue ->
        if (invoiceRepository.invoiceExistsFor(invoiceIssue.orderId)) {
            val message = ErrorMessage("The invoice of order ${invoiceIssue.orderId.value} is already issued.")
            throw InvoiceAlreadyIssuedException(message)
        }
    }

    private fun invoiceIsIssuedCondition() = PostCondition<InvoiceIssue> { invoiceIssue ->
        if (invoiceRepository.invoiceIsMissingFor(invoiceIssue.orderId)) {
            throw InvoiceIssueFailed(ErrorMessage("The invoice of order ${invoiceIssue.orderId.value} was not issued."))
        }
    }
}
