package org.flexstore.infra.spring.adapter.rest.json

import org.flexstore.domain.order.*
import org.flexstore.domain.settings.PaymentDelay
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class JsonOrder(
    @JsonProperty("id") val id: String,
    @JsonProperty("placedAt") val placedAt: String,
    @JsonProperty("status") val status: String,
    @JsonProperty("lines") val lines: List<JsonOrderLine>,
    @JsonProperty("totalPrice") val totalPrice: BigDecimal,
    @JsonProperty("currency") val currency: String,
    @JsonProperty("deliveryAddress") val deliveryAddress: JsonPostalAddress,
    @JsonProperty("billingAddress") val billingAddress: JsonPostalAddress,
    // Only while the order awaits its payment
    @JsonProperty("paymentDeadline") val paymentDeadline: String?
) {
    constructor(order: Order) : this(
        order.orderId.value,
        order.placedAt.toString(),
        order.status.name,
        order.lines.asList().map { JsonOrderLine.of(it) },
        order.totalPrice().amount.value,
        order.totalPrice().currency.symbol,
        JsonPostalAddress.of(order.addresses.delivery),
        JsonPostalAddress.of(order.addresses.billing),
        null
    )

    // The order seen by its customer, who is told by when to pay it
    constructor(order: Order, paymentDelay: PaymentDelay) : this(
        order.orderId.value,
        order.placedAt.toString(),
        order.status.name,
        order.lines.asList().map { JsonOrderLine.of(it) },
        order.totalPrice().amount.value,
        order.totalPrice().currency.symbol,
        JsonPostalAddress.of(order.addresses.delivery),
        JsonPostalAddress.of(order.addresses.billing),
        paymentDeadlineOf(order, paymentDelay)
    )
}

private fun paymentDeadlineOf(order: Order, paymentDelay: PaymentDelay): String? =
    if (order.isAwaitingPayment()) order.paymentDeadline(paymentDelay).value.toString() else null

data class JsonOrderLine(
    @JsonProperty("productId") val productId: String,
    @JsonProperty("productName") val productName: String,
    @JsonProperty("unitPrice") val unitPrice: BigDecimal,
    @JsonProperty("quantity") val quantity: Int,
    @JsonProperty("linePrice") val linePrice: BigDecimal
) {
    companion object {
        fun of(orderLine: OrderLine): JsonOrderLine {
            val unitPrice = orderLine.unitPrice.amount.value
            val linePrice = orderLine.linePrice().amount.value
            val productId = orderLine.productId.id.value
            val productName = orderLine.productName.value
            return JsonOrderLine(productId, productName, unitPrice, orderLine.quantity.value, linePrice)
        }
    }
}
