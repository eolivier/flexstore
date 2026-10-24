package org.flexstore.domain.cart

import org.flexstore.domain.cart.model.*

interface CartRepository {

    // The cart of the owner, empty when it has none yet
    fun load(owner: CartOwner): Cart

    // Stores the cart as it is: its draft items get an id, the items it no longer holds are deleted
    fun save(cart: Cart): Cart

    fun delete(owner: CartOwner)
}
