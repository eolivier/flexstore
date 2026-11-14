package org.flexstore.domain.restock

import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.restock.usecase.RestockUseCases
import org.ucop.domain.isNotNominal

// Domain service, run by the system user: the customers who wait for a product that is back in stock are
// notified by email. Called regularly by the infrastructure, which covers every way the stock comes back (an
// administrator changes it, an unpaid order is cancelled, an order is refunded)
class RestockNotificationService(
    private val actorResolutionService: ActorResolutionService,
    private val restockUseCases: RestockUseCases
) {

    // The subscriptions whose customer was notified; a customer who cannot be notified is left aside
    fun notifyRestockedProducts(): RestockSubscriptions {
        val system = actorResolutionService.system()
        val restockedSubscriptions = restockUseCases.getRestockedSubscriptions(system).asList()
        return RestockSubscriptions(restockedSubscriptions.filter { subscription -> isNotified(subscription) })
    }

    private fun isNotified(restockSubscription: RestockSubscription): Boolean {
        val system = actorResolutionService.system()
        return runCatching { restockUseCases.notify(system, restockSubscription) }
            .onFailure { failure -> if (failure.stopsTheRun()) throw failure }
            .isSuccess
    }

    // A customer who cannot be notified is left aside; a failure that is not nominal, or the refusal of the
    // system itself, stops the run
    private fun Throwable.stopsTheRun() = isNotNominal() || cause is RestockNotificationForbiddenException
}
