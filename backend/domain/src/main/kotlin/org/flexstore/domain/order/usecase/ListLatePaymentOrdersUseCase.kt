package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.*
import org.ucop.domain.entity.*

// The orders whose payment is late at a time limit. Read by the domain service that cancels them, on behalf of the
// system: no user runs this journey
class ListLatePaymentOrdersUseCase(private val orderRepository: OrderRepository) :
    QueryUseCase<PaymentTimeLimit, Orders> {

    override fun getPreConditions(): PreConditions<PaymentTimeLimit> = PreConditions.none()

    override fun getNominalQuery() = NominalQuery<PaymentTimeLimit, Orders> { timeLimit ->
        orderRepository.findAll().latePaymentsAt(timeLimit)
    }

    override fun getPostConditions(): PostConditions<Orders> = PostConditions.none()
}
