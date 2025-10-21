package org.flexstore.infra.spring.adapter.mapping

import org.flexstore.domain.valueobject.CartItem
import org.flexstore.domain.valueobject.Items
import org.flexstore.infra.spring.adapter.rest.json.JsonCartItem
import org.flexstore.infra.spring.adapter.rest.json.JsonCartItems

fun Items.toJsonCartItems() = JsonCartItems(
    this.items.map {
        when (it) {
            is CartItem -> JsonCartItem(it)
            else -> throw IllegalArgumentException("Unknown item type: ${it::class.java}")
        }
    },
    this.totalPrice().amount.value,
    this.currency().symbol
)