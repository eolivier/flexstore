package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.product.model.ProductId
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.stock.*
import org.flexstore.domain.stock.usecase.StockUseCases
import org.flexstore.infra.spring.adapter.rest.json.JsonStock
import org.flexstore.infra.spring.adapter.rest.json.JsonStockQuantity
import org.springframework.web.bind.annotation.*
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt

// The stocks of the products: read by everyone, given by the administrators (ADMIN role, see SecurityConfig)
@RestController
class StockController(
    private val stockUseCases: StockUseCases,
    private val actorResolutionService: ActorResolutionService
) {

    @GetMapping("/api/stocks")
    fun getStocks(): List<JsonStock> {
        val stocks = stockUseCases.getStocks(actorResolutionService.visitor())
        return stocks.asList().map { JsonStock.of(it) }
    }

    @GetMapping("/api/stocks/{productId}")
    fun getStock(@PathVariable productId: String): JsonStock =
        JsonStock.of(stockUseCases.getStock(actorResolutionService.visitor(), ProductId(Identity(productId))))

    @PutMapping("/api/admin/stocks/{productId}")
    fun changeStock(
        @AuthenticationPrincipal editor: Jwt,
        @PathVariable productId: String,
        @RequestBody stockQuantity: JsonStockQuantity
    ): JsonStock {
        val stock = Stock(ProductId(Identity(productId)), StockQuantity(stockQuantity.quantity))
        val actor = actorResolutionService.actorOf(editor)
        return JsonStock.of(stockUseCases.change(actor, stock))
    }
}
