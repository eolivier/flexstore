package org.flexstore.domain.payment.usecase

import org.flexstore.domain.actor.customerId
import org.flexstore.domain.actor.verifyCanAct
import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.OrderNotFoundException
import org.flexstore.domain.payment.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A customer asks whether the payment of one of their orders was completed on the payment provider
class VerifyPaymentUseCase(
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository
) : QueryUseCase<OrderPayment, Boolean> {

    override fun getPreConditions(): PreConditions<OrderPayment> =
        PreConditions(customerCanActCondition(), orderOfTheCustomerCondition())

    override fun getNominalQuery() = NominalQuery<OrderPayment, Boolean> { orderPayment ->
        paymentRepository.isCompleted(orderPayment.orderId)
    }

    override fun getPostConditions(): PostConditions<Boolean> = PostConditions.none()

    // The customer, resolved before the use case runs, must be a customer or an admin who can act
    private fun customerCanActCondition() = PreCondition<OrderPayment> { orderPayment ->
        orderPayment.customer.verifyCanAct()
    }

    // The order of another customer is reported as not found, so that its existence is not revealed
    private fun orderOfTheCustomerCondition() = PreCondition<OrderPayment> { orderPayment ->
        val orderId = orderPayment.orderId
        val customerId = orderPayment.customer.customerId()
        if (orderRepository.orderIsMissing(orderId) || orderRepository.get(orderId).doesNotBelongTo(customerId)) {
            throw OrderNotFoundException(ErrorMessage("Order ${orderPayment.orderId} not found."))
        }
    }
}
