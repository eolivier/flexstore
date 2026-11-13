package org.flexstore.domain.cart.usecase

import org.flexstore.domain.actor.verifyCanAct
import org.flexstore.domain.cart.CartOwner
import org.ucop.domain.entity.Actor

// What the preconditions of the cart use cases check first: the owner of a customer cart is an actor who can
// act (resolved before the use case runs); the cart of a visitor needs no account
fun Actor.verifyCanUseCartOf(owner: CartOwner) {
    if (owner is CartOwner.Customer) {
        verifyCanAct()
    }
}
