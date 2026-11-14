package org.flexstore.domain.restock.usecase

import org.flexstore.domain.actor.customerId
import org.flexstore.domain.actor.verifyCanAct
import org.flexstore.domain.restock.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A customer no longer wants to be notified when a product is back in stock: leaving a product they do not wait
// for changes nothing
class UnsubscribeFromRestockUseCase(
    private val restockSubscriptionRepository: RestockSubscriptionRepository
) : UseCase<RestockUnsubscribing> {

    override fun getPreConditions(): PreConditions<RestockUnsubscribing> = PreConditions(customerCanActCondition())

    override fun getNominalScenario(): NominalScenario<RestockUnsubscribing> {
        val unsubscribeStep = Step<RestockUnsubscribing> { unsubscribing ->
            restockSubscriptionRepository.remove(unsubscribing.customer.customerId(), unsubscribing.productId)
        }
        return NominalScenario(unsubscribeStep)
    }

    override fun getPostConditions(): PostConditions<RestockUnsubscribing> =
        PostConditions(customerIsNotSubscribedCondition())

    // The customer, resolved before the use case runs, must be a customer or an admin who can act
    private fun customerCanActCondition() = PreCondition<RestockUnsubscribing> { unsubscribing ->
        unsubscribing.customer.verifyCanAct()
    }

    private fun customerIsNotSubscribedCondition() = PostCondition<RestockUnsubscribing> { unsubscribing ->
        if (restockSubscriptionRepository.contains(unsubscribing.customer.customerId(), unsubscribing.productId)) {
            val message = ErrorMessage("The customer is still subscribed to ${unsubscribing.productId}.")
            throw RestockUnsubscriptionFailed(message)
        }
    }
}
