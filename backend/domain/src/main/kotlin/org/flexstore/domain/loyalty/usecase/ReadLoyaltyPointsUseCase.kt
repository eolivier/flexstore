package org.flexstore.domain.loyalty.usecase

import org.flexstore.domain.loyalty.LoyaltyPoints
import org.flexstore.domain.order.OrderRepository
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.repository.UserRepository
import org.ucop.domain.entity.*

// The points of a customer: one for each whole euro or dollar of their paid orders, so that an order
// refunded gives its points back
class ReadLoyaltyPointsUseCase(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository
) : QueryUseCase<UserId.ValidUserId, LoyaltyPoints> {

    override fun getPreConditions(): PreConditions<UserId.ValidUserId> = PreConditions(customerCanActCondition())

    override fun getNominalQuery() = NominalQuery<UserId.ValidUserId, LoyaltyPoints> { customer ->
        LoyaltyPoints.earnedFor(orderRepository.findAllBy(customer).wholeUnitsSpent())
    }

    override fun getPostConditions(): PostConditions<LoyaltyPoints> = PostConditions.none()

    private fun customerCanActCondition() = PreCondition<UserId.ValidUserId> { customer ->
        userRepository.verifyCustomerCanAct(customer)
    }
}
