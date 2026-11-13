package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.stock.*

// The number of units that the shop has of a product
data class JsonStock(
    @JsonProperty("productId") val productId: String,
    @JsonProperty("quantity") val quantity: Int
) {
    companion object {
        fun of(stock: Stock) = JsonStock(stock.productId.identity.value, stock.quantity.value)
    }
}

// The new stock of a product, given by an administrator
data class JsonStockQuantity @JsonCreator constructor(@JsonProperty("quantity") val quantity: Int)

// The number of units at or under which the administrators are warned that a product runs low
data class JsonStockAlert @JsonCreator constructor(
    @JsonProperty("productId") val productId: String,
    @JsonProperty("threshold") val threshold: Int
) {
    companion object {
        fun of(stockAlert: StockAlert) =
            JsonStockAlert(stockAlert.productId.identity.value, stockAlert.threshold.value)
    }
}

// The new alert threshold of a product, given by an administrator
data class JsonAlertThreshold @JsonCreator constructor(@JsonProperty("threshold") val threshold: Int)

// A stock that reached the threshold of its product
data class JsonLowStock(
    @JsonProperty("productId") val productId: String,
    @JsonProperty("quantity") val quantity: Int,
    @JsonProperty("threshold") val threshold: Int
) {
    companion object {
        fun of(lowStock: LowStock): JsonLowStock {
            val (stock, threshold) = lowStock
            return JsonLowStock(stock.productId.identity.value, stock.quantity.value, threshold.value)
        }
    }
}
