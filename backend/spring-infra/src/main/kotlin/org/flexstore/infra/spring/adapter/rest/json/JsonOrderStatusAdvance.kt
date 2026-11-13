package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.*
import org.flexstore.domain.order.usecase.OrderStatusAdvance
import org.ucop.domain.entity.Actor
import org.ucop.domain.ErrorMessage

// The status of the order when the administrator asks to move it to the next one; moving it to shipped may give
// the carrier and the tracking number of the parcel, together or not at all
data class JsonOrderStatusAdvance @JsonCreator constructor(
    @JsonProperty("currentStatus") val currentStatus: String,
    @JsonProperty("carrier") val carrier: String?,
    @JsonProperty("trackingNumber") val trackingNumber: String?
) {
    fun toOrderStatusAdvance(editor: Actor, orderId: OrderId): OrderStatusAdvance {
        val tracking = OrderTracking.typed(carrier.orEmpty(), trackingNumber.orEmpty())
        return OrderStatusAdvance(editor, orderId, OrderStatus.valueOf(currentStatus), tracking)
    }
}

// The carrier and the tracking number that an administrator gives to an order shipped
data class JsonOrderTrackingCorrection @JsonCreator constructor(
    @JsonProperty("carrier") val carrier: String,
    @JsonProperty("trackingNumber") val trackingNumber: String
) {
    fun toOrderTrackingCorrection(editor: Actor, orderId: OrderId): OrderTrackingCorrection {
        val tracking = OrderTracking.typed(carrier, trackingNumber)
        val given = tracking as? OrderTracking.Given
            ?: throw InvalidOrderTrackingException(ErrorMessage("A carrier and a tracking number are given."))
        return OrderTrackingCorrection(editor, orderId, given)
    }
}
