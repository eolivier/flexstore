package org.flexstore.domain.invoice.usecase

import org.flexstore.domain.invoice.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.usecase.*

// The use cases that refund an order and tell its customer: reading it back, then sending them an email
data class RefundedOrderUseCases(
    val readAdministeredOrderUseCase: ReadAdministeredOrderUseCase,
    val sendOrderStatusEmailUseCase: SendOrderStatusEmailUseCase
)

// Domain service: an administrator cancels and refunds a paid order before it is shipped. Its payment is
// given back and its units return to the stock; its invoice is cancelled by a credit note; the order is read
// back, then its customer is told by email, all through the use cases. When a step is refused, nothing of the
// refund is kept (the steps run in one transaction)
class OrderRefundService(
    private val refundOrderUseCase: RefundOrderUseCase,
    private val issueCreditNoteUseCase: IssueCreditNoteUseCase,
    private val refundedOrderUseCases: RefundedOrderUseCases
) {
    fun refund(orderRefund: OrderRefund): AdministeredOrder {
        refundOrderUseCase.unfold(orderRefund)
        val creditNoteIssue = CreditNoteIssue(orderRefund.orderId, CreditNoteDate.now())
        issueCreditNoteUseCase.unfold(CreditNoteIssuance(orderRefund.editor, creditNoteIssue))
        val orderReading = AdministeredOrderReading(orderRefund.editor, orderRefund.orderId)
        val refundedOrder = refundedOrderUseCases.readAdministeredOrderUseCase.unfold(orderReading)
        refundedOrderUseCases.sendOrderStatusEmailUseCase.unfold(refundedOrder.order)
        return refundedOrder
    }
}
