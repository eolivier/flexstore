package org.flexstore.domain.restock.usecase

import org.flexstore.domain.actor.cannotRunScheduledUseCases
import org.flexstore.domain.email.EmailRepository
import org.flexstore.domain.email.model.RestockEmail
import org.flexstore.domain.product.ProductRepository
import org.flexstore.domain.restock.*
import org.flexstore.domain.stock.StockRepository
import org.flexstore.domain.user.model.User
import org.flexstore.domain.user.repository.UserRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A customer who waits for a product is told by email that it is back in stock, once: their subscription ends
// with the email. A customer whose account cannot act is not notified
class NotifyRestockUseCase(
    private val restockSubscriptionRepository: RestockSubscriptionRepository,
    private val productRepository: ProductRepository,
    private val stockRepository: StockRepository,
    private val userRepository: UserRepository,
    private val emailRepository: EmailRepository
) : UseCase<RestockNotifying> {

    override fun getPreConditions(): PreConditions<RestockNotifying> =
        PreConditions(
            notifierCanRunScheduledUseCasesCondition(), customerCanActCondition(), productIsInStockCondition()
        )

    // The email is sent first: a failure leaves the subscription to be notified at the next run
    override fun getNominalScenario(): NominalScenario<RestockNotifying> {
        val sendEmailStep = Step<RestockNotifying> { notifying ->
            val subscription = notifying.restockSubscription
            val customer = userRepository.findById(subscription.customer) as User.DefinedUser
            val product = productRepository.get(subscription.productId)
            emailRepository.send(RestockEmail(customer.email, customer.fullName, product))
        }
        val endSubscriptionStep = Step<RestockNotifying> { notifying ->
            val subscription = notifying.restockSubscription
            restockSubscriptionRepository.remove(subscription.customer, subscription.productId)
        }
        return NominalScenario(listOf(sendEmailStep, endSubscriptionStep))
    }

    override fun getPostConditions(): PostConditions<RestockNotifying> = PostConditions(subscriptionIsEndedCondition())

    // Only an Admin or the system notifies the customers that a scheduler finds
    private fun notifierCanRunScheduledUseCasesCondition() = PreCondition<RestockNotifying> { notifying ->
        if (notifying.notifier.cannotRunScheduledUseCases()) {
            val message = ErrorMessage("Only an administrator or the system can notify the return of a product.")
            throw RestockNotificationForbiddenException(message)
        }
    }

    private fun customerCanActCondition() = PreCondition<RestockNotifying> { notifying ->
        userRepository.verifyCustomerCanAct(notifying.restockSubscription.customer)
    }

    private fun productIsInStockCondition() = PreCondition<RestockNotifying> { notifying ->
        val productId = notifying.restockSubscription.productId
        if (stockRepository.stockOf(productId).isOutOfStock()) {
            throw RestockNotificationRefusedException(ErrorMessage("The product $productId is still out of stock."))
        }
    }

    private fun subscriptionIsEndedCondition() = PostCondition<RestockNotifying> { notifying ->
        val subscription = notifying.restockSubscription
        if (restockSubscriptionRepository.contains(subscription.customer, subscription.productId)) {
            val message = ErrorMessage("The subscription of ${subscription.customer.value} was not ended.")
            throw RestockNotificationFailed(message)
        }
    }
}
