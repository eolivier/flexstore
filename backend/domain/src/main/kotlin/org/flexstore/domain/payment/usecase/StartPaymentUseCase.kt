package org.flexstore.domain.payment.usecase

import org.flexstore.domain.order.*
import org.flexstore.domain.payment.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.UserRepository
import org.flexstore.domain.user.model.User
import org.ucop.domain.entity.*

// A customer starts the payment of one of their orders, awaiting its payment, on the payment provider
class StartPaymentUseCase(
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository,
    private val userRepository: UserRepository
) : UseCase<OrderPayment> {

    override fun getPreConditions(): PreConditions<OrderPayment> =
        PreConditions(
            orderOfTheCustomerCondition(), orderAwaitsPaymentCondition(), orderIsNotCancelledCondition()
        )

    override fun getNominalScenario(): NominalScenario<OrderPayment> {
        val startStep = Step<OrderPayment> { orderPayment ->
            val order = orderRepository.findById(orderPayment.orderId)!!
            val customer = userRepository.findById(orderPayment.customer.userId) as User.DefinedUser
            paymentRepository.start(PaymentRequest(order, Payer.of(customer, order)))
        }
        return NominalScenario(startStep)
    }

    override fun getPostConditions(): PostConditions<OrderPayment> = PostConditions(sessionIsKeptCondition())

    // The order of another customer is reported as not found, so that its existence is not revealed
    private fun orderOfTheCustomerCondition() = PreCondition<OrderPayment> { orderPayment ->
        val order = orderRepository.findById(orderPayment.orderId)
        if (order == null || order.belongsTo(orderPayment.customer.userId).not()) {
            throw OrderNotFoundException(ErrorMessage("Order ${orderPayment.orderId.value} not found."))
        }
    }

    private fun orderAwaitsPaymentCondition() = PreCondition<OrderPayment> { orderPayment ->
        val order = orderRepository.findById(orderPayment.orderId)!!
        if (order.isPaid()) {
            throw OrderAlreadyPaidException(ErrorMessage("Order ${order.number()} is already paid."))
        }
    }

    // Not paid in time: the customer orders again
    private fun orderIsNotCancelledCondition() = PreCondition<OrderPayment> { orderPayment ->
        val order = orderRepository.findById(orderPayment.orderId)!!
        if (order.isCancelled()) {
            val message = ErrorMessage("Order ${order.number()} was cancelled: it was not paid in time.")
            throw OrderCancelledException(message)
        }
    }

    private fun sessionIsKeptCondition() = PostCondition<OrderPayment> { orderPayment ->
        val orderId = orderPayment.orderId
        if (paymentRepository.sessionIsMissingFor(orderId)) {
            val message = ErrorMessage("The payment of order ${orderId.value} did not start.")
            throw PaymentNotStartedException(message)
        }
    }
}
