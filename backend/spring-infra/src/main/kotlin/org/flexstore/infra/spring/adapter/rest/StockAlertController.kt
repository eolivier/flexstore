package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.product.model.ProductId
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.stock.*
import org.flexstore.domain.stock.usecase.StockAlertUseCases
import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.infra.spring.adapter.rest.json.JsonAlertThreshold
import org.flexstore.infra.spring.adapter.rest.json.JsonLowStock
import org.flexstore.infra.spring.adapter.rest.json.JsonStockAlert
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

// The alerts on the stocks, for the administrators (ADMIN role, see SecurityConfig, checked again by the use
// cases): the threshold of each product, and the stocks that reached it
@RestController
class StockAlertController(
    private val stockAlertUseCases: StockAlertUseCases,
    private val actorResolutionService: ActorResolutionService
) {

    @GetMapping("/api/admin/stocks/low")
    fun getLowStocks(@AuthenticationPrincipal reader: Jwt): List<JsonLowStock> {
        val actor = actorResolutionService.actorOf(reader)
        return stockAlertUseCases.lowStocks(actor).asList().map { JsonLowStock.of(it) }
    }

    @GetMapping("/api/admin/stocks/{productId}/alert")
    fun getStockAlert(@AuthenticationPrincipal reader: Jwt, @PathVariable productId: String): JsonStockAlert {
        val actor = actorResolutionService.actorOf(reader)
        return JsonStockAlert.of(stockAlertUseCases.alertOf(actor, ProductId(Identity(productId))))
    }

    @PutMapping("/api/admin/stocks/{productId}/alert")
    fun changeStockAlert(
        @AuthenticationPrincipal editor: Jwt,
        @PathVariable productId: String,
        @RequestBody alertThreshold: JsonAlertThreshold
    ): JsonStockAlert {
        val stockAlert = StockAlert(ProductId(Identity(productId)), AlertThreshold(alertThreshold.threshold))
        val actor = actorResolutionService.actorOf(editor)
        return JsonStockAlert.of(stockAlertUseCases.change(actor, stockAlert))
    }
}
