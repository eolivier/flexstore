package org.flexstore.domain.restock.usecase

import org.flexstore.domain.actor.customerId
import org.flexstore.domain.actor.verifyCanAct
import org.flexstore.domain.restock.RestockSubscriptionRepository
import org.flexstore.domain.restock.RestockSubscriptions
import org.ucop.domain.entity.*

// The products a customer waits for, to show them which ones they will be notified of
class ListCustomerRestockSubscriptionsUseCase(
    private val restockSubscriptionRepository: RestockSubscriptionRepository
) : QueryUseCase<Actor, RestockSubscriptions> {

    override fun getPreConditions(): PreConditions<Actor> = PreConditions(customerCanActCondition())

    override fun getNominalQuery() = NominalQuery<Actor, RestockSubscriptions> { customer ->
        restockSubscriptionRepository.findAllBy(customer.customerId())
    }

    override fun getPostConditions(): PostConditions<RestockSubscriptions> = PostConditions.none()

    // The customer, resolved before the use case runs, must be a customer or an admin who can act
    private fun customerCanActCondition() = PreCondition<Actor> { customer -> customer.verifyCanAct() }
}
