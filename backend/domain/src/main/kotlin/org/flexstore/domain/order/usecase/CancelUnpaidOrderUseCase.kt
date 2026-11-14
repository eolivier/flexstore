package org.flexstore.domain.order.usecase

import org.flexstore.domain.actor.cannotRunScheduledUseCases
import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.*
import org.flexstore.domain.payment.PaymentRepository
import org.flexstore.domain.stock.StockRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An order not paid within the payment delay is cancelled: its payment can no longer complete, and its reserved
// units go back to the stock. If the customer paid in the meantime, it is not cancelled
class CancelUnpaidOrderUseCase(
    private val orderRepository: OrderRepository,
    private val stockRepository: StockRepository,
    private val paymentRepository: PaymentRepository
) : UseCase<UnpaidOrderCancellation> {

    override fun getPreConditions(): PreConditions<UnpaidOrderCancellation> =
        PreConditions(
            actorCanRunScheduledUseCasesCondition(), orderExistsCondition(), orderAwaitsPaymentCondition(),
            timeLimitIsExceededCondition()
        )

    // The payment is stopped first: when the customer has just paid, it fails and the order stays as it is
    override fun getNominalScenario(): NominalScenario<UnpaidOrderCancellation> {
        val stopPaymentStep = Step<UnpaidOrderCancellation> { cancellation ->
            paymentRepository.stop(cancellation.orderId)
        }
        val cancelStep = Step<UnpaidOrderCancellation> { cancellation ->
            val cancelledAt = StatusChangedAt(cancellation.timeLimit.checkedAt)
            orderRepository.save(order(cancellation).cancelledAt(cancelledAt))
        }
        val returnStockStep = Step<UnpaidOrderCancellation> { cancellation ->
            order(cancellation).lines.asList().forEach { line ->
                stockRepository.release(line.productId, line.quantity)
            }
        }
        return NominalScenario(listOf(stopPaymentStep, cancelStep, returnStockStep))
    }

    override fun getPostConditions(): PostConditions<UnpaidOrderCancellation> =
        PostConditions(orderIsCancelledCondition())

    // Only called once the order is known
    private fun order(cancellation: UnpaidOrderCancellation) = orderRepository.get(cancellation.orderId)

    // Only an Admin or the system cancels the orders that a scheduler finds late
    private fun actorCanRunScheduledUseCasesCondition() = PreCondition<UnpaidOrderCancellation> { cancellation ->
        if (cancellation.actor.cannotRunScheduledUseCases()) {
            throw UnpaidOrderForbiddenException(
                ErrorMessage("Only an administrator or the system can cancel an unpaid order.")
            )
        }
    }

    private fun orderExistsCondition() = PreCondition<UnpaidOrderCancellation> { cancellation ->
        if (orderRepository.orderIsMissing(cancellation.orderId)) {
            throw OrderNotFoundException(ErrorMessage("Order ${cancellation.orderId} not found."))
        }
    }

    private fun orderAwaitsPaymentCondition() = PreCondition<UnpaidOrderCancellation> { cancellation ->
        val order = order(cancellation)
        if (order.isNotAwaitingPayment()) {
            val orderNumber = order.number()
            val message = ErrorMessage("Order $orderNumber is ${order.status}: it does not await its payment.")
            throw OrderNotAwaitingPaymentException(message)
        }
    }

    private fun timeLimitIsExceededCondition() = PreCondition<UnpaidOrderCancellation> { cancellation ->
        val order = order(cancellation)
        if (cancellation.timeLimit.isNotExceededFor(order)) {
            val message = ErrorMessage("Order ${order.number()} can still be paid.")
            throw PaymentTimeLimitNotExceededException(message)
        }
    }

    private fun orderIsCancelledCondition() = PostCondition<UnpaidOrderCancellation> { cancellation ->
        if (orderRepository.get(cancellation.orderId).isNotCancelled()) {
            val message = ErrorMessage("Order ${cancellation.orderId} was not cancelled.")
            throw OrderCancellationFailed(message)
        }
    }
}
