package org.flexstore.domain.restock.usecase

import org.flexstore.domain.actor.customerId
import org.flexstore.domain.actor.verifyCanAct
import org.flexstore.domain.product.ProductRepository
import org.flexstore.domain.product.model.ProductNotFoundException
import org.flexstore.domain.restock.*
import org.flexstore.domain.stock.StockRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A customer asks to be notified by email when a product out of stock is back: asking again changes nothing
class SubscribeToRestockUseCase(
    private val restockSubscriptionRepository: RestockSubscriptionRepository,
    private val productRepository: ProductRepository,
    private val stockRepository: StockRepository
) : UseCase<RestockSubscribing> {

    override fun getPreConditions(): PreConditions<RestockSubscribing> =
        PreConditions(customerCanActCondition(), productExistsCondition(), productIsOutOfStockCondition())

    override fun getNominalScenario(): NominalScenario<RestockSubscribing> {
        val subscribeStep = Step<RestockSubscribing> { subscribing ->
            val customer = subscribing.customer.customerId()
            if (restockSubscriptionRepository.doesNotContain(customer, subscribing.productId)) {
                val restockSubscription = RestockSubscription(
                    RestockSubscriptionId.nextId(), customer, subscribing.productId, SubscribedAt.now()
                )
                restockSubscriptionRepository.save(restockSubscription)
            }
        }
        return NominalScenario(subscribeStep)
    }

    override fun getPostConditions(): PostConditions<RestockSubscribing> =
        PostConditions(customerIsSubscribedCondition())

    // The customer, resolved before the use case runs, must be a customer or an admin who can act
    private fun customerCanActCondition() = PreCondition<RestockSubscribing> { subscribing ->
        subscribing.customer.verifyCanAct()
    }

    private fun productExistsCondition() = PreCondition<RestockSubscribing> { subscribing ->
        if (productRepository.productIsMissing(subscribing.productId)) {
            val message = ErrorMessage("The product ${subscribing.productId} does not exist.")
            throw ProductNotFoundException(message)
        }
    }

    // Nothing to wait for when the product is in stock
    private fun productIsOutOfStockCondition() = PreCondition<RestockSubscribing> { subscribing ->
        if (stockRepository.stockOf(subscribing.productId).isInStock()) {
            throw RestockSubscriptionRefusedException("The product ${subscribing.productId} is in stock.")
        }
    }

    private fun customerIsSubscribedCondition() = PostCondition<RestockSubscribing> { subscribing ->
        if (restockSubscriptionRepository.doesNotContain(subscribing.customer.customerId(), subscribing.productId)) {
            val message = ErrorMessage("The customer was not subscribed to ${subscribing.productId}.")
            throw RestockSubscriptionFailed(message)
        }
    }
}
