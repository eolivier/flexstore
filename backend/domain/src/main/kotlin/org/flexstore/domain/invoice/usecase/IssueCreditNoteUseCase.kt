package org.flexstore.domain.invoice.usecase

import org.flexstore.domain.invoice.*
import org.flexstore.domain.order.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.entity.*

// A refunded order gets a credit note once, which cancels its invoice, with the next number of the sequence of
// the credit notes
class IssueCreditNoteUseCase(
    private val orderRepository: OrderRepository,
    private val invoiceRepository: InvoiceRepository,
    private val creditNoteRepository: CreditNoteRepository
) : UseCase<CreditNoteIssue> {

    override fun getPreConditions(): PreConditions<CreditNoteIssue> =
        PreConditions(orderIsRefundedCondition(), orderIsInvoicedCondition(), creditNoteIsNotIssuedCondition())

    override fun getNominalScenario(): NominalScenario<CreditNoteIssue> =
        NominalScenario(Step { creditNoteIssue -> creditNoteRepository.issue(creditNoteIssue) })

    override fun getPostConditions(): PostConditions<CreditNoteIssue> =
        PostConditions(creditNoteIsIssuedCondition())

    private fun orderIsRefundedCondition() = PreCondition<CreditNoteIssue> { creditNoteIssue ->
        val orderId = creditNoteIssue.orderId
        if (orderRepository.findById(orderId)?.isRefunded() != true) {
            throw OrderNotRefundedException(ErrorMessage("Order ${orderId.value} is not refunded: no credit note."))
        }
    }

    // The credit note cancels the invoice of the order
    private fun orderIsInvoicedCondition() = PreCondition<CreditNoteIssue> { creditNoteIssue ->
        val orderId = creditNoteIssue.orderId
        if (invoiceRepository.invoiceIsMissingFor(orderId)) {
            throw InvoiceNotFoundException(ErrorMessage("Order ${orderId.value} has no invoice to cancel."))
        }
    }

    private fun creditNoteIsNotIssuedCondition() = PreCondition<CreditNoteIssue> { creditNoteIssue ->
        val orderId = creditNoteIssue.orderId
        if (creditNoteRepository.creditNoteExistsFor(orderId)) {
            val message = ErrorMessage("The credit note of order ${orderId.value} is already issued.")
            throw CreditNoteAlreadyIssuedException(message)
        }
    }

    private fun creditNoteIsIssuedCondition() = PostCondition<CreditNoteIssue> { creditNoteIssue ->
        val orderId = creditNoteIssue.orderId
        if (creditNoteRepository.creditNoteIsMissingFor(orderId)) {
            val message = ErrorMessage("The credit note of order ${orderId.value} was not issued.")
            throw CreditNoteIssueFailed(message)
        }
    }
}
