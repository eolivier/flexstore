package org.flexstore.domain.invoice.usecase

import org.flexstore.domain.invoice.*
import org.flexstore.domain.order.OrderId
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// The credit note of an order, issued when it was refunded
class ReadCreditNoteUseCase(
    private val creditNoteRepository: CreditNoteRepository
) : QueryUseCase<OrderId, CreditNote> {

    override fun getPreConditions(): PreConditions<OrderId> = PreConditions(creditNoteExistsCondition())

    override fun getNominalQuery() = NominalQuery<OrderId, CreditNote> { orderId ->
        creditNoteRepository.findBy(orderId)!!
    }

    override fun getPostConditions(): PostConditions<CreditNote> = PostConditions.none()

    private fun creditNoteExistsCondition() = PreCondition<OrderId> { orderId ->
        if (creditNoteRepository.creditNoteIsMissingFor(orderId)) {
            val message = ErrorMessage("Order ${orderId.value} has no credit note: it is not refunded.")
            throw CreditNoteNotFoundException(message)
        }
    }
}
