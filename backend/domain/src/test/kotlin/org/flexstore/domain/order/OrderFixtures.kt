package org.flexstore.domain.order

import org.flexstore.domain.order.model.Order
import org.flexstore.domain.order.model.OrderStatus
import org.flexstore.domain.order.model.OrderStatusChange
import org.flexstore.domain.order.model.StatusChangedAt

// The order at the given status, reached when it was placed: for the tests that start from that status
fun Order.withStatus(status: OrderStatus): Order {
    val statusChange = OrderStatusChange(status, StatusChangedAt(placedAt))
    return copy(history = history.with(statusChange))
}
