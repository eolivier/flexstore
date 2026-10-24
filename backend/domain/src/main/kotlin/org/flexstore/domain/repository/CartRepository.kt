package org.flexstore.domain.repository

import org.flexstore.domain.entity.Cart

interface CartRepository {

    fun load(): Cart

    // Stores the cart as it is: its draft items get an id, the items it no longer holds are deleted
    fun save(cart: Cart): Cart
}
