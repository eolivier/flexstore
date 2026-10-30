package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.settings.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.infra.spring.adapter.rest.json.JsonFreeShippingThreshold
import org.flexstore.infra.spring.adapter.rest.json.JsonPaymentDelay
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
    fun enableOrderConfirmationEmail() =
        JsonShopSettings(shopSettingsAdministrationService.enableOrderConfirmationEmail())

    @Operation(summary = "Stop sending an email to the customer when an order is placed")
    @PostMapping("/order-confirmation-email/disable")
    fun disableOrderConfirmationEmail() =
        JsonShopSettings(shopSettingsAdministrationService.disableOrderConfirmationEmail())

    @Operation(summary = "Change the time a customer has to pay an order (1 to 1440 minutes); unpaid, it is cancelled")
    @PutMapping("/payment-delay")
    fun changePaymentDelay(@RequestBody paymentDelay: JsonPaymentDelay) =
        JsonShopSettings(shopSettingsAdministrationService.changePaymentDelay(paymentDelay.toPaymentDelay()))

    @Operation(summary = "Change the price of the items from which the standard shipping is free (0: always free)")
    @PutMapping("/free-shipping-threshold")
    fun changeFreeShippingThreshold(@RequestBody threshold: JsonFreeShippingThreshold): JsonShopSettings {
        val administration = shopSettingsAdministrationService
        val changedSettings = administration.changeFreeShippingThreshold(threshold.toFreeShippingThreshold())
        return JsonShopSettings(changedSettings)
    }
}
