package org.flexstore.infra.repository

import org.flexstore.domain.cart.*
import org.flexstore.domain.cart.model.*
import org.flexstore.domain.shared.*
import java.util.concurrent.atomic.AtomicInteger

class InMemoryCartRepository : CartRepository {

    private val counter = AtomicInteger(0)

    private val carts = mutableMapOf<CartOwner, Cart>()

    override fun load(owner: CartOwner): Cart = carts[owner] ?: Cart(owner)

    override fun save(cart: Cart): Cart {
        val savedCart = cart.copy(items = cart.items.map {
            when (it) {
                is DraftItem -> CartItem(ItemId(Identity("item-" + counter.incrementAndGet())), it.product, it.quantity)
                else -> it
            }
        })
        carts[cart.owner] = savedCart
        return savedCart
    }

    override fun delete(owner: CartOwner) {
        carts.remove(owner)
    }
}
