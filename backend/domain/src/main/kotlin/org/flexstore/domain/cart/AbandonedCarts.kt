package org.flexstore.domain.cart

import java.time.Instant
import org.flexstore.domain.settings.AbandonedCartDelay
import org.ucop.domain.ErrorMessage
import org.ucop.domain.NominalException
import org.ucop.domain.PreConditionException
import org.ucop.domain.entity.Actor

// The moment before which a cart must have been last changed to be abandoned: the delay of the shop settings
// before now
data class AbandonmentLimit(val value: Instant) {
    companion object {
        fun of(abandonedCartDelay: AbandonedCartDelay, now: Instant) =
            AbandonmentLimit(now - abandonedCartDelay.asDuration())
    }
}

// The carts of customers, not empty, left unchanged since the limit and not reminded since
data class AbandonedCarts(private val carts: List<Cart>) {

    constructor(vararg carts: Cart) : this(carts.toList())

    fun isEmpty() = carts.isEmpty()

    // Read-only view for the services and the adapters that go through the carts
    fun asList(): List<Cart> = carts.toList()
}

// The customer of the cart is the only one who is reminded of it
data class AbandonedCartReminding(val actor: Actor, val customer: CartOwner.Customer)

// The abandoned carts are read by an actor, as of a limit
data class AbandonedCartsReading(val reader: Actor, val limit: AbandonmentLimit)

data class AbandonedCartForbiddenException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)

data class AbandonedCartReminderRefusedException(override val errorMessage: ErrorMessage) :
    PreConditionException(errorMessage)
data class AbandonedCartReminderFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
