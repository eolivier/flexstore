package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.product.ProductId
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.stock.*
import org.flexstore.domain.stock.usecase.StockUseCases
import org.flexstore.infra.spring.adapter.rest.json.JsonStock
import org.flexstore.infra.spring.adapter.rest.json.JsonStockQuantity
import org.springframework.web.bind.annotation.*

// The stocks of the products: read by everyone, given by the administrators (ADMIN role, see SecurityConfig)
@Tag(name = "Stocks")
@RestController
class StockController(private val stockUseCases: StockUseCases) {

    @Operation(summary = "Retrieve the stocks of the products (a product without stock has none)")
    @GetMapping("/api/stocks")
    fun getStocks(): List<JsonStock> = stockUseCases.getStocks().asList().map { JsonStock.of(it) }

    @Operation(summary = "Retrieve the stock of a product (0 when it was never given)")
    @GetMapping("/api/stocks/{productId}")
    fun getStock(@PathVariable productId: String): JsonStock =
        JsonStock.of(stockUseCases.getStock(ProductId(Identity(productId))))

    @Operation(summary = "Give the number of units of a product that the shop has (administrator)")
    @PutMapping("/api/admin/stocks/{productId}")
    fun changeStock(@PathVariable productId: String, @RequestBody stockQuantity: JsonStockQuantity): JsonStock {
        val stock = Stock(ProductId(Identity(productId)), StockQuantity(stockQuantity.quantity))
        return JsonStock.of(stockUseCases.change(stock))
    }
}
