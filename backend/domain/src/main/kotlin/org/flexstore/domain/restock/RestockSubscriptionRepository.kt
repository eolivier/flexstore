package org.flexstore.domain.restock

import org.flexstore.domain.product.model.ProductId
import org.flexstore.domain.user.model.UserId

// What the customers subscribed to, to be notified when a product is back in stock
interface RestockSubscriptionRepository {

    fun save(restockSubscription: RestockSubscription)

    fun remove(customer: UserId.ValidUserId, productId: ProductId)

    // The customer already waits for the return of the product
    fun contains(customer: UserId.ValidUserId, productId: ProductId): Boolean

    fun doesNotContain(customer: UserId.ValidUserId, productId: ProductId) = !contains(customer, productId)

    // The subscriptions of the customer
    fun findAllBy(customer: UserId.ValidUserId): RestockSubscriptions

    // The subscriptions of every customer
    fun findAll(): RestockSubscriptions
}
