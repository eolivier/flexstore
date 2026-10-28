package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.order.*

// The status of the order when the administrator asks to cancel and refund it
data class JsonOrderRefund @JsonCreator constructor(
    @JsonProperty("currentStatus") val currentStatus: String
) {
    fun toOrderRefund(orderId: OrderId) = OrderRefund(orderId, OrderStatus.valueOf(currentStatus))
}
