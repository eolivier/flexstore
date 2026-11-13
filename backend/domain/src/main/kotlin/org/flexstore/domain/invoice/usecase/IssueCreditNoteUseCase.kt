package org.flexstore.domain.invoice.usecase

import org.flexstore.domain.invoice.*
import org.flexstore.domain.invoice.model.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.*
import org.flexstore.domain.user.repository.UserRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator who refunded an order issues its credit note: a refunded order gets a credit note once,
// which cancels its invoice, with the next number of the sequence of the credit notes
class IssueCreditNoteUseCase(
    private val orderRepository: OrderRepository,
    private val invoiceRepository: InvoiceRepository,
    private val creditNoteRepository: CreditNoteRepository,
    private val userRepository: UserRepository
) : UseCase<CreditNoteIssuance> {

    override fun getPreConditions(): PreConditions<CreditNoteIssuance> = PreConditions(
        issuerIsAdministratorCondition(),
        orderIsRefundedCondition(),
        orderIsInvoicedCondition(),
        creditNoteIsNotIssuedCondition()
    )

    override fun getNominalScenario(): NominalScenario<CreditNoteIssuance> =
        NominalScenario(Step { (_, creditNoteIssue) -> creditNoteRepository.issue(creditNoteIssue) })

    override fun getPostConditions(): PostConditions<CreditNoteIssuance> =
        PostConditions(creditNoteIsIssuedCondition())

    // The issuer must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun issuerIsAdministratorCondition() = PreCondition<CreditNoteIssuance> { request ->
        if (userRepository.isNotAdministrator(request.issuer)) {
            throw CreditNoteForbiddenException(ErrorMessage("Only an administrator can issue a credit note."))
        }
    }

    private fun orderIsRefundedCondition() = PreCondition<CreditNoteIssuance> { (_, creditNoteIssue) ->
        val orderId = creditNoteIssue.orderId
        if (orderRepository.findById(orderId)?.isRefunded() != true) {
            throw OrderNotRefundedException(
                ErrorMessage("Order ${orderId.value} is not refunded: no credit note.")
            )
        }
    }

    // The credit note cancels the invoice of the order
    private fun orderIsInvoicedCondition() = PreCondition<CreditNoteIssuance> { (_, creditNoteIssue) ->
        val orderId = creditNoteIssue.orderId
        if (invoiceRepository.invoiceIsMissingFor(orderId)) {
            throw InvoiceNotFoundException(ErrorMessage("Order ${orderId.value} has no invoice to cancel."))
        }
    }

    private fun creditNoteIsNotIssuedCondition() = PreCondition<CreditNoteIssuance> { (_, creditNoteIssue) ->
        val orderId = creditNoteIssue.orderId
        if (creditNoteRepository.creditNoteExistsFor(orderId)) {
            val message = ErrorMessage("The credit note of order ${orderId.value} is already issued.")
            throw CreditNoteAlreadyIssuedException(message)
        }
    }

    private fun creditNoteIsIssuedCondition() = PostCondition<CreditNoteIssuance> { (_, creditNoteIssue) ->
        val orderId = creditNoteIssue.orderId
        if (creditNoteRepository.creditNoteIsMissingFor(orderId)) {
            val message = ErrorMessage("The credit note of order ${orderId.value} was not issued.")
            throw CreditNoteIssueFailed(message)
        }
    }
}
