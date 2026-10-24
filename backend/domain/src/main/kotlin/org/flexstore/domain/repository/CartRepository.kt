package org.flexstore.domain.repository

import org.flexstore.domain.entity.Cart
import org.flexstore.domain.entity.CartOwner

interface CartRepository {

    // The cart of the owner, empty when it has none yet
    fun load(owner: CartOwner): Cart

    // Stores the cart as it is: its draft items get an id, the items it no longer holds are deleted
    fun save(cart: Cart): Cart

    fun delete(owner: CartOwner)
}
