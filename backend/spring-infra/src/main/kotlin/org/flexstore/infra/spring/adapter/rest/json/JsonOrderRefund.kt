package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.*
import org.ucop.domain.entity.Actor

// The status of the order when the administrator asks to cancel and refund it
data class JsonOrderRefund @JsonCreator constructor(
    @JsonProperty("currentStatus") val currentStatus: String
) {
    fun toOrderRefund(editor: Actor, orderId: OrderId) =
        OrderRefund(editor, orderId, OrderStatus.valueOf(currentStatus))
}
