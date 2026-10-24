package org.flexstore.infra.repository

import org.flexstore.domain.entity.Cart
import org.flexstore.domain.repository.CartRepository
import org.flexstore.domain.valueobject.*
import java.util.concurrent.atomic.AtomicInteger

class InMemoryCartRepository : CartRepository {

    private val counter = AtomicInteger(0)

    private var cart = Cart()

    override fun load(): Cart = cart

    override fun save(cart: Cart): Cart {
        this.cart = Cart(cart.items.map {
            when (it) {
                is DraftItem -> CartItem(ItemId(Identity("item-" + counter.incrementAndGet())), it.product, it.quantity)
                else -> it
            }
        })
        return this.cart
    }
}
