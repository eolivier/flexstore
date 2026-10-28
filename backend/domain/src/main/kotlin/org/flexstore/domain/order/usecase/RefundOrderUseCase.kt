package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*
import org.flexstore.domain.payment.PaymentRepository
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.stock.StockRepository
import org.ucop.domain.entity.*

// An administrator cancels a paid order before it is shipped: its payment is given back to the customer, and its
// units go back to the stock.
// The initial status is checked before: the order still has the status the administrator saw, and it can be
// refunded. The final status is checked after: the order is refunded
class RefundOrderUseCase(
    private val orderRepository: OrderRepository,
    private val stockRepository: StockRepository,
    private val paymentRepository: PaymentRepository
) : UseCase<OrderRefund> {

    override fun getPreConditions(): PreConditions<OrderRefund> =
        PreConditions(orderExistsCondition(), initialStatusIsCurrentCondition(), orderIsRefundableCondition())

    // The payment is refunded first: when the provider refuses it, the order stays as it is
    override fun getNominalScenario(): NominalScenario<OrderRefund> {
        val refundPaymentStep = Step<OrderRefund> { orderRefund ->
            paymentRepository.refund(orderRefund.orderId)
        }
        val refundOrderStep = Step<OrderRefund> { orderRefund ->
            orderRepository.save(order(orderRefund).refunded())
        }
        val returnStockStep = Step<OrderRefund> { orderRefund ->
            order(orderRefund).lines.asList().forEach { line ->
                stockRepository.release(line.productId, line.quantity)
            }
        }
        return NominalScenario(listOf(refundPaymentStep, refundOrderStep, returnStockStep))
    }

    override fun getPostConditions(): PostConditions<OrderRefund> = PostConditions(orderIsRefundedCondition())

    // Only called once the order is known
    private fun order(orderRefund: OrderRefund) = orderRepository.findById(orderRefund.orderId)!!

    private fun orderExistsCondition() = PreCondition<OrderRefund> { orderRefund ->
        if (orderRepository.orderIsMissing(orderRefund.orderId)) {
            throw OrderNotFoundException(ErrorMessage("Order ${orderRefund.orderId.value} not found."))
        }
    }

    // Another administrator may have changed the status in the meantime
    private fun initialStatusIsCurrentCondition() = PreCondition<OrderRefund> { orderRefund ->
        val order = order(orderRefund)
        if (order.status != orderRefund.currentStatus) {
            val message = "Order ${order.number()} is ${order.status}, not ${orderRefund.currentStatus}."
            throw OrderStatusChangedException(ErrorMessage(message))
        }
    }

    private fun orderIsRefundableCondition() = PreCondition<OrderRefund> { orderRefund ->
        if (orderRefund.currentStatus.isNotRefundable()) {
            val message = "An order ${orderRefund.currentStatus} cannot be cancelled and refunded."
            throw OrderNotRefundableException(ErrorMessage(message))
        }
    }

    private fun orderIsRefundedCondition() = PostCondition<OrderRefund> { orderRefund ->
        if (orderRepository.findById(orderRefund.orderId)?.isRefunded() != true) {
            val message = ErrorMessage("Order ${orderRefund.orderId.value} was not refunded.")
            throw OrderRefundFailed(message)
        }
    }
}
