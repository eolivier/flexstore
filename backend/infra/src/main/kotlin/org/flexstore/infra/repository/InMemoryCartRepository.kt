package org.flexstore.infra.repository

import org.flexstore.domain.cart.*
import org.flexstore.domain.sharedkernel.*
import java.time.Instant
import java.util.concurrent.atomic.AtomicInteger

class InMemoryCartRepository : CartRepository {

    private val counter = AtomicInteger(0)

    private val carts = mutableMapOf<CartOwner, Cart>()
    private val lastChanges = mutableMapOf<CartOwner, Instant>()
    private val remindedOwners = mutableSetOf<CartOwner>()

    override fun load(owner: CartOwner): Cart = carts[owner] ?: Cart(owner)

    override fun save(cart: Cart): Cart {
        val savedCart = cart.copy(items = Items(cart.items.asList().map {
            when (it) {
                is DraftItem -> CartItem(ItemId(Identity("item-" + counter.incrementAndGet())), it.product, it.quantity)
                else -> it
            }
        }))
        carts[cart.owner] = savedCart
        lastChanges[cart.owner] = InstantProvider.now()
        remindedOwners.remove(cart.owner)
        return savedCart
    }

    override fun delete(owner: CartOwner) {
        carts.remove(owner)
        lastChanges.remove(owner)
        remindedOwners.remove(owner)
    }

    override fun abandonedCartsAt(limit: AbandonmentLimit): AbandonedCarts {
        val abandonedCarts = carts.values.filter { cart ->
            val owner = cart.owner
            owner is CartOwner.Customer && cart.hasItems() && owner !in remindedOwners &&
                lastChanges.getValue(owner) <= limit.value
        }
        return AbandonedCarts(abandonedCarts)
    }

    override fun markReminded(customer: CartOwner.Customer) {
        remindedOwners.add(customer)
    }

    override fun isReminded(customer: CartOwner.Customer) = customer in remindedOwners
}
