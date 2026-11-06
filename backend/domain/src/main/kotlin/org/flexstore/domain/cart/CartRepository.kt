package org.flexstore.domain.cart

interface CartRepository {

    // The cart of the owner, empty when it has none yet
    fun load(owner: CartOwner): Cart

    // Stores the cart as it is: its draft items get an id, the items it no longer holds are deleted
    fun save(cart: Cart): Cart

    fun delete(owner: CartOwner)

    // The carts of the customers that hold items, were last changed before the limit and were not reminded since
    // their last change (the visitors have no email: they are never reminded)
    fun abandonedCartsAt(limit: AbandonmentLimit): AbandonedCarts

    // The customer is reminded of their cart until it changes again
    fun markReminded(customer: CartOwner.Customer)

    fun isReminded(customer: CartOwner.Customer): Boolean

    fun isNotReminded(customer: CartOwner.Customer): Boolean = !isReminded(customer)
}
