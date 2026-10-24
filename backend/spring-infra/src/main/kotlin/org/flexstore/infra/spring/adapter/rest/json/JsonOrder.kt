package org.flexstore.infra.spring.adapter.rest.json

import org.flexstore.domain.order.*
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class JsonOrder(
    @JsonProperty("id") val id: String,
    @JsonProperty("placedAt") val placedAt: String,
    @JsonProperty("status") val status: String,
    @JsonProperty("lines") val lines: List<JsonOrderLine>,
    @JsonProperty("totalPrice") val totalPrice: BigDecimal,
    @JsonProperty("currency") val currency: String
) {
    constructor(order: Order) : this(
        order.orderId.value,
        order.placedAt.toString(),
        order.status.name,
        order.lines.asList().map {
            JsonOrderLine(it.productId.id.value, it.productName.value, it.unitPrice.amount.value, it.quantity.value, it.linePrice().amount.value)
        },
        order.totalPrice().amount.value,
        order.totalPrice().currency.symbol
    )
}

data class JsonOrderLine(
    @JsonProperty("productId") val productId: String,
    @JsonProperty("productName") val productName: String,
    @JsonProperty("unitPrice") val unitPrice: BigDecimal,
    @JsonProperty("quantity") val quantity: Int,
    @JsonProperty("linePrice") val linePrice: BigDecimal
)
