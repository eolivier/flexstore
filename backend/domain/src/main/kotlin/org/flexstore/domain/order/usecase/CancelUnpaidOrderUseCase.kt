package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*
import org.flexstore.domain.payment.PaymentRepository
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.stock.StockRepository
import org.ucop.domain.entity.*

// An order not paid within the payment delay is cancelled: its payment can no longer complete, and its reserved
// units go back to the stock. If the customer paid in the meantime, it is not cancelled
class CancelUnpaidOrderUseCase(
    private val orderRepository: OrderRepository,
    private val stockRepository: StockRepository,
    private val paymentRepository: PaymentRepository
) : UseCase<UnpaidOrderCancellation> {

    override fun getPreConditions(): PreConditions<UnpaidOrderCancellation> =
        PreConditions(orderExistsCondition(), orderAwaitsPaymentCondition(), timeLimitIsExceededCondition())

    // The payment is stopped first: when the customer has just paid, it fails and the order stays as it is
    override fun getNominalScenario(): NominalScenario<UnpaidOrderCancellation> {
        val stopPaymentStep = Step<UnpaidOrderCancellation> { cancellation ->
            paymentRepository.stop(cancellation.orderId)
        }
        val cancelStep = Step<UnpaidOrderCancellation> { cancellation ->
            orderRepository.save(order(cancellation).cancelled())
        }
        val returnStockStep = Step<UnpaidOrderCancellation> { cancellation ->
            order(cancellation).lines.asList().forEach { line ->
                stockRepository.save(stockRepository.stockOf(line.productId).returned(line.quantity))
            }
        }
        return NominalScenario(listOf(stopPaymentStep, cancelStep, returnStockStep))
    }

    override fun getPostConditions(): PostConditions<UnpaidOrderCancellation> =
        PostConditions(orderIsCancelledCondition())

    // Only called once the order is known
    private fun order(cancellation: UnpaidOrderCancellation) = orderRepository.findById(cancellation.orderId)!!

    private fun orderExistsCondition() = PreCondition<UnpaidOrderCancellation> { cancellation ->
        if (orderRepository.orderIsMissing(cancellation.orderId)) {
            throw OrderNotFoundException(ErrorMessage("Order ${cancellation.orderId.value} not found."))
        }
    }

    private fun orderAwaitsPaymentCondition() = PreCondition<UnpaidOrderCancellation> { cancellation ->
        val order = order(cancellation)
        if (!order.isAwaitingPayment()) {
            val message = ErrorMessage("Order ${order.number()} is ${order.status}: it does not await its payment.")
            throw OrderNotAwaitingPaymentException(message)
        }
    }

    private fun timeLimitIsExceededCondition() = PreCondition<UnpaidOrderCancellation> { cancellation ->
        val order = order(cancellation)
        if (!cancellation.timeLimit.isExceededFor(order)) {
            val message = ErrorMessage("Order ${order.number()} can still be paid.")
            throw PaymentTimeLimitNotExceededException(message)
        }
    }

    private fun orderIsCancelledCondition() = PostCondition<UnpaidOrderCancellation> { cancellation ->
        if (orderRepository.findById(cancellation.orderId)?.isCancelled() != true) {
            val message = ErrorMessage("Order ${cancellation.orderId.value} was not cancelled.")
            throw OrderCancellationFailed(message)
        }
    }
}
