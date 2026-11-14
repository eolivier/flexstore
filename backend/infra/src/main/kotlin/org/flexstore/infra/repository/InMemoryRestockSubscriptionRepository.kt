package org.flexstore.infra.repository

import org.flexstore.domain.product.model.ProductId
import org.flexstore.domain.restock.RestockSubscription
import org.flexstore.domain.restock.RestockSubscriptionRepository
import org.flexstore.domain.restock.RestockSubscriptions
import org.flexstore.domain.user.model.UserId

// The subscriptions to the return of a product in memory: for the tests
class InMemoryRestockSubscriptionRepository : RestockSubscriptionRepository {

    private val restockSubscriptions = mutableListOf<RestockSubscription>()

    override fun save(restockSubscription: RestockSubscription) {
        restockSubscriptions.add(restockSubscription)
    }

    override fun remove(customer: UserId.ValidUserId, productId: ProductId) {
        restockSubscriptions.removeIf { it.customer == customer && it.productId == productId }
    }

    override fun contains(customer: UserId.ValidUserId, productId: ProductId) =
        restockSubscriptions.any { it.customer == customer && it.productId == productId }

    override fun findAllBy(customer: UserId.ValidUserId) =
        RestockSubscriptions(restockSubscriptions.filter { it.customer == customer })

    override fun findAll() = RestockSubscriptions(restockSubscriptions.toList())
}
