package org.flexstore.domain.cart

import org.flexstore.domain.actor.ActorResolutionService
import org.ucop.domain.entity.Actor

// The actor behind the owner of a cart in the tests: the customer of a customer cart, a visitor otherwise
fun ActorResolutionService.actorOf(owner: CartOwner): Actor = when (owner) {
    is CartOwner.Customer -> actorOf(owner.userId)
    is CartOwner.Visitor -> visitor()
}
