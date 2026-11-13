package org.flexstore.domain.loyalty.usecase

import org.flexstore.domain.loyalty.LoyaltyStatement
import org.flexstore.domain.order.OrderRepository
import org.flexstore.domain.actor.customerId
import org.flexstore.domain.actor.verifyCanAct
import org.ucop.domain.entity.*

// The points of a customer: a line for each paid order, with one point for each whole euro or dollar of it,
// so that an order refunded gives its points back
class ReadLoyaltyPointsUseCase(
    private val orderRepository: OrderRepository
) : QueryUseCase<Actor, LoyaltyStatement> {

    override fun getPreConditions(): PreConditions<Actor> = PreConditions(customerCanActCondition())

    override fun getNominalQuery() = NominalQuery<Actor, LoyaltyStatement> { customer ->
        LoyaltyStatement.of(orderRepository.findAllBy(customer.customerId()))
    }

    override fun getPostConditions(): PostConditions<LoyaltyStatement> = PostConditions.none()

    // The customer, resolved before the use case runs, must be a customer or an admin who can act
    private fun customerCanActCondition() = PreCondition<Actor> { customer -> customer.verifyCanAct() }
}
