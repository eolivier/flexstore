package org.flexstore.domain.order

import org.flexstore.domain.user.model.*

// The customer who placed an order, as the administrators see it: who to contact about it
data class OrderCustomer(val fullName: FullName, val email: Email) {
    companion object {
        fun of(user: User) = when (user) {
            is User.DefinedUser -> OrderCustomer(user.fullName, user.email)
            is User.DraftUser, is User.UndefinedUser -> unknown()
        }

        // The account of the customer cannot be read any more
        fun unknown() = OrderCustomer(FullName.none(), Email(""))
    }
}

// An order followed by the administrators: the order and the customer who placed it
data class AdministeredOrder(val order: Order, val customer: OrderCustomer)

data class AdministeredOrders(private val orders: List<AdministeredOrder>) {

    constructor(vararg orders: AdministeredOrder) : this(orders.toList())

    fun asList(): List<AdministeredOrder> = orders.toList()
}
