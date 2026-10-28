package org.flexstore.domain.order

// The order at the given status, reached when it was placed: for the tests that start from that status
fun Order.withStatus(status: OrderStatus): Order {
    val statusChange = OrderStatusChange(status, StatusChangedAt(placedAt))
    return copy(history = history.with(statusChange))
}
