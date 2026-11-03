package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.sales.DayCount
import org.flexstore.domain.sales.SalesDashboardReading
import org.flexstore.domain.sales.SalesQuestion
import org.flexstore.domain.sales.usecase.ReadSalesDashboardUseCase
import org.flexstore.domain.sharedkernel.InstantProvider
import org.flexstore.infra.spring.adapter.rest.json.JsonSalesDashboard
import org.springframework.web.bind.annotation.*
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.of
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt

// The sales of the shop, for the administrators (ADMIN role, see SecurityConfig)
@Tag(name = "Administration")
@RestController
@RequestMapping("/api/admin/sales-dashboard")
class AdminSalesController(private val readSalesDashboardUseCase: ReadSalesDashboardUseCase) {

    @Operation(summary = "Retrieve the revenue, the orders per day and the best sellers of the last 7, 30 or 90 days")
    @GetMapping
    fun getSalesDashboard(
        @AuthenticationPrincipal reader: Jwt,
        @RequestParam(defaultValue = "30") days: Int
    ): JsonSalesDashboard {
        val question = SalesQuestion(DayCount(days), InstantProvider.now())
        val salesDashboardReading = SalesDashboardReading(UserId.of(reader.subject), question)
        return JsonSalesDashboard(readSalesDashboardUseCase.unfold(salesDashboardReading))
    }
}
