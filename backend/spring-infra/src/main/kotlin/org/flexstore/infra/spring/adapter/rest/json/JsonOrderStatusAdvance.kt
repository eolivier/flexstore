package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.order.*

// The status of the order when the administrator asks to move it to the next one
data class JsonOrderStatusAdvance @JsonCreator constructor(
    @JsonProperty("currentStatus") val currentStatus: String
) {
    fun toOrderStatusAdvance(orderId: OrderId) = OrderStatusAdvance(orderId, OrderStatus.valueOf(currentStatus))
}
