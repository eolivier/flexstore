package org.flexstore.domain.restock

import org.flexstore.domain.product.model.ProductId
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.UserId
import org.ucop.domain.ErrorMessage
import org.ucop.domain.PostConditionException
import org.ucop.domain.PreConditionException
import org.ucop.domain.entity.Actor
import java.time.Instant

data class RestockSubscriptionId(override val id: Identity) : Identifiable() {
    companion object {
        fun nextId() = RestockSubscriptionId(Identity.nextId())
    }
}

// The moment a customer asked to be notified of the return of a product
data class SubscribedAt(val value: Instant) {
    companion object {
        fun now() = SubscribedAt(InstantProvider.now())
    }
}

// A customer who waits for a product out of stock, to be notified by email as soon as it is back in stock
data class RestockSubscription(
    val restockSubscriptionId: RestockSubscriptionId,
    val customer: UserId.ValidUserId,
    val productId: ProductId,
    val subscribedAt: SubscribedAt
)

// The subscriptions to the return of a product, the first subscribed first
data class RestockSubscriptions(private val restockSubscriptions: List<RestockSubscription>) {

    constructor(vararg restockSubscriptions: RestockSubscription) : this(restockSubscriptions.toList())

    fun isEmpty() = restockSubscriptions.isEmpty()

    // Read-only view for the adapters (JSON) and the domain service that notifies them one by one
    fun asList(): List<RestockSubscription> = restockSubscriptions.sortedBy { it.subscribedAt.value }
}

// A customer who asks to be notified when a product is back in stock
data class RestockSubscribing(val customer: Actor, val productId: ProductId)

// A customer who no longer wants to be notified when a product is back in stock
data class RestockUnsubscribing(val customer: Actor, val productId: ProductId)

// The system or an administrator who reads the subscriptions whose product is back in stock
data class RestockedSubscriptionsReading(val reader: Actor)

// The system or an administrator who notifies a customer that the product they wait for is back in stock
data class RestockNotifying(val notifier: Actor, val restockSubscription: RestockSubscription)

data class RestockSubscriptionRefusedException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage) {
    constructor(message: String) : this(ErrorMessage(message))
}

data class RestockNotificationForbiddenException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)

data class RestockNotificationRefusedException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)

data class RestockSubscriptionFailed(override val errorMessage: ErrorMessage) :
    PostConditionException(errorMessage)

data class RestockUnsubscriptionFailed(override val errorMessage: ErrorMessage) :
    PostConditionException(errorMessage)

data class RestockNotificationFailed(override val errorMessage: ErrorMessage) :
    PostConditionException(errorMessage)
