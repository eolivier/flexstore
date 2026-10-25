package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.stock.Stock

// The number of units that the shop has of a product
data class JsonStock(
    @JsonProperty("productId") val productId: String,
    @JsonProperty("quantity") val quantity: Int
) {
    companion object {
        fun of(stock: Stock) = JsonStock(stock.productId.id.value, stock.quantity.value)
    }
}

// The new stock of a product, given by an administrator
data class JsonStockQuantity @JsonCreator constructor(@JsonProperty("quantity") val quantity: Int)
