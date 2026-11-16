package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.sales.DayCount
import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.sales.SalesDashboardReading
import org.flexstore.domain.sales.SalesQuestion
import org.flexstore.domain.sales.usecase.ReadSalesDashboardUseCase
import org.flexstore.domain.sharedkernel.InstantProvider
import org.flexstore.infra.spring.adapter.rest.json.JsonSalesDashboard
import org.springframework.web.bind.annotation.*
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt

// The sales of the shop, for the administrators (ADMIN role, see SecurityConfig)
@RestController
@RequestMapping("/api/admin/sales-dashboard")
class AdminSalesController(
    private val readSalesDashboardUseCase: ReadSalesDashboardUseCase,
    private val actorResolutionService: ActorResolutionService
) {

    @GetMapping
    fun getSalesDashboard(
        @AuthenticationPrincipal reader: Jwt,
        @RequestParam(defaultValue = "30") days: Int
    ): JsonSalesDashboard {
        val question = SalesQuestion(DayCount(days), InstantProvider.now())
        val actor = actorResolutionService.actorOf(reader)
        val salesDashboardReading = SalesDashboardReading(actor, question)
        return JsonSalesDashboard(readSalesDashboardUseCase.unfold(salesDashboardReading))
    }
}
