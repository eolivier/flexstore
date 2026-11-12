package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.product.model.ProductId
import org.flexstore.domain.sharedkernel.Identity
import org.flexstore.domain.stock.*
import org.flexstore.domain.stock.usecase.StockAlertUseCases
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.of
import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.infra.spring.adapter.rest.json.JsonAlertThreshold
import org.flexstore.infra.spring.adapter.rest.json.JsonLowStock
import org.flexstore.infra.spring.adapter.rest.json.JsonStockAlert
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

// The alerts on the stocks, for the administrators (ADMIN role, see SecurityConfig, checked again by the use
// cases): the threshold of each product, and the stocks that reached it
@Tag(name = "Stocks")
@RestController
class StockAlertController(
    private val stockAlertUseCases: StockAlertUseCases,
    private val actorResolutionService: ActorResolutionService
) {

    @Operation(summary = "Retrieve the stocks at or under the alert threshold of their product, from the lowest")
    @GetMapping("/api/admin/stocks/low")
    fun getLowStocks(@AuthenticationPrincipal reader: Jwt): List<JsonLowStock> =
        stockAlertUseCases.lowStocks(UserId.of(reader.subject)).asList().map { JsonLowStock.of(it) }

    @Operation(summary = "Retrieve the alert threshold of a product (the default one when it was never given)")
    @GetMapping("/api/admin/stocks/{productId}/alert")
    fun getStockAlert(@AuthenticationPrincipal reader: Jwt, @PathVariable productId: String): JsonStockAlert {
        val actor = actorResolutionService.actorOf(UserId.of(reader.subject))
        return JsonStockAlert.of(stockAlertUseCases.alertOf(actor, ProductId(Identity(productId))))
    }

    @Operation(summary = "Give the number of units at or under which the administrators are warned")
    @PutMapping("/api/admin/stocks/{productId}/alert")
    fun changeStockAlert(
        @AuthenticationPrincipal editor: Jwt,
        @PathVariable productId: String,
        @RequestBody alertThreshold: JsonAlertThreshold
    ): JsonStockAlert {
        val stockAlert = StockAlert(ProductId(Identity(productId)), AlertThreshold(alertThreshold.threshold))
        val actor = actorResolutionService.actorOf(UserId.of(editor.subject))
        return JsonStockAlert.of(stockAlertUseCases.change(actor, stockAlert))
    }
}
