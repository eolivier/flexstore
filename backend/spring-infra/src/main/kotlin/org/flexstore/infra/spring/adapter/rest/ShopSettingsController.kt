package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.settings.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.infra.spring.adapter.rest.json.JsonShopSettings
import org.springframework.web.bind.annotation.*

// The settings of the shop, for the administrators (ADMIN role, see SecurityConfig)
@Tag(name = "Administration")
@RestController
@RequestMapping("/api/admin/settings")
class ShopSettingsController(private val shopSettingsAdministrationService: ShopSettingsAdministrationService) {

    @Operation(summary = "Retrieve the settings of the shop")
    @GetMapping
    fun getSettings() = JsonShopSettings(shopSettingsAdministrationService.getSettings())

    @Operation(summary = "Send an email to the customer when an order is placed")
    @PostMapping("/order-confirmation-email/enable")
    fun enableOrderConfirmationEmail() = JsonShopSettings(shopSettingsAdministrationService.enableOrderConfirmationEmail())

    @Operation(summary = "Stop sending an email to the customer when an order is placed")
    @PostMapping("/order-confirmation-email/disable")
    fun disableOrderConfirmationEmail() = JsonShopSettings(shopSettingsAdministrationService.disableOrderConfirmationEmail())
}
