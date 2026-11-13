package org.flexstore.domain.payment.usecase

import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.OrderNotFoundException
import org.flexstore.domain.payment.*
import org.flexstore.domain.user.repository.UserRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A customer asks whether the payment of one of their orders was completed on the payment provider
class VerifyPaymentUseCase(
    private val orderRepository: OrderRepository,
    private val paymentRepository: PaymentRepository,
    private val userRepository: UserRepository
) : QueryUseCase<OrderPayment, Boolean> {

    override fun getPreConditions(): PreConditions<OrderPayment> =
        PreConditions(customerCanActCondition(), orderOfTheCustomerCondition())

    override fun getNominalQuery() = NominalQuery<OrderPayment, Boolean> { orderPayment ->
        paymentRepository.isCompleted(orderPayment.orderId)
    }

    override fun getPostConditions(): PostConditions<Boolean> = PostConditions.none()

    private fun customerCanActCondition() = PreCondition<OrderPayment> { orderPayment ->
        userRepository.verifyCustomerCanAct(orderPayment.customer.userId)
    }

    // The order of another customer is reported as not found, so that its existence is not revealed
    private fun orderOfTheCustomerCondition() = PreCondition<OrderPayment> { orderPayment ->
        val order = orderRepository.findById(orderPayment.orderId)
        if (order == null || order.belongsTo(orderPayment.customer.userId).not()) {
            throw OrderNotFoundException(ErrorMessage("Order ${orderPayment.orderId} not found."))
        }
    }
}
