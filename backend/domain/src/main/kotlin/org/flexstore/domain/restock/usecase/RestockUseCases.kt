package org.flexstore.domain.restock.usecase

import org.flexstore.domain.product.model.ProductId
import org.flexstore.domain.restock.*
import org.ucop.domain.entity.Actor

// The use cases of the subscriptions to the return of a product
class RestockUseCases(
    private val subscribeToRestockUseCase: SubscribeToRestockUseCase,
    private val listCustomerRestockSubscriptionsUseCase: ListCustomerRestockSubscriptionsUseCase,
    private val listRestockedSubscriptionsUseCase: ListRestockedSubscriptionsUseCase,
    private val notifyRestockUseCase: NotifyRestockUseCase
) {
    fun subscribe(customer: Actor, productId: ProductId) =
        subscribeToRestockUseCase.unfold(RestockSubscribing(customer, productId))

    fun getSubscriptions(customer: Actor) = listCustomerRestockSubscriptionsUseCase.unfold(customer)

    fun getRestockedSubscriptions(reader: Actor) =
        listRestockedSubscriptionsUseCase.unfold(RestockedSubscriptionsReading(reader))

    fun notify(notifier: Actor, restockSubscription: RestockSubscription) =
        notifyRestockUseCase.unfold(RestockNotifying(notifier, restockSubscription))
}
