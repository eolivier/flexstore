package org.flexstore.domain.loyalty.usecase

import org.flexstore.domain.loyalty.LoyaltyPoints
import org.flexstore.domain.order.OrderRepository
import org.flexstore.domain.actor.customerId
import org.flexstore.domain.actor.verifyCanAct
import org.ucop.domain.entity.*

// The points of a customer: one for each whole euro or dollar of their paid orders, so that an order
// refunded gives its points back
class ReadLoyaltyPointsUseCase(
    private val orderRepository: OrderRepository
) : QueryUseCase<Actor, LoyaltyPoints> {

    override fun getPreConditions(): PreConditions<Actor> = PreConditions(customerCanActCondition())

    override fun getNominalQuery() = NominalQuery<Actor, LoyaltyPoints> { customer ->
        LoyaltyPoints.earnedFor(orderRepository.findAllBy(customer.customerId()).wholeUnitsSpent())
    }

    override fun getPostConditions(): PostConditions<LoyaltyPoints> = PostConditions.none()

    // The customer, resolved before the use case runs, must be a customer or an admin who can act
    private fun customerCanActCondition() = PreCondition<Actor> { customer -> customer.verifyCanAct() }
}
