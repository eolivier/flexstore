package org.flexstore.domain.restock.usecase

import org.flexstore.domain.actor.cannotRunScheduledUseCases
import org.flexstore.domain.restock.*
import org.flexstore.domain.stock.StockRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// The subscriptions whose product is back in stock: the customers to notify
class ListRestockedSubscriptionsUseCase(
    private val restockSubscriptionRepository: RestockSubscriptionRepository,
    private val stockRepository: StockRepository
) : QueryUseCase<RestockedSubscriptionsReading, RestockSubscriptions> {

    override fun getPreConditions(): PreConditions<RestockedSubscriptionsReading> =
        PreConditions(readerCanRunScheduledUseCasesCondition())

    override fun getNominalQuery() = NominalQuery<RestockedSubscriptionsReading, RestockSubscriptions> {
        val subscriptions = restockSubscriptionRepository.findAll().asList()
        RestockSubscriptions(subscriptions.filter { stockRepository.stockOf(it.productId).isInStock() })
    }

    override fun getPostConditions(): PostConditions<RestockSubscriptions> = PostConditions.none()

    // Only an Admin or the system reads what a scheduler finds
    private fun readerCanRunScheduledUseCasesCondition() = PreCondition<RestockedSubscriptionsReading> { reading ->
        if (reading.reader.cannotRunScheduledUseCases()) {
            val message = ErrorMessage("Only an administrator or the system can read the restocked subscriptions.")
            throw RestockNotificationForbiddenException(message)
        }
    }
}
