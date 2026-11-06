package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.settings.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.of
import org.flexstore.infra.spring.adapter.rest.json.JsonAbandonedCartDelay
import org.flexstore.infra.spring.adapter.rest.json.JsonFreeShippingThreshold
import org.flexstore.infra.spring.adapter.rest.json.JsonPaymentDelay
import org.flexstore.infra.spring.adapter.rest.json.JsonShopSettings
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

// The settings of the shop, for the administrators (ADMIN role, see SecurityConfig, checked again by the use
// cases)
@Tag(name = "Administration")
@RestController
@RequestMapping("/api/admin/settings")
class ShopSettingsController(private val shopSettingsAdministrationService: ShopSettingsAdministrationService) {

    @Operation(summary = "Retrieve the settings of the shop")
    @GetMapping
    fun getSettings(@AuthenticationPrincipal reader: Jwt) =
        JsonShopSettings(shopSettingsAdministrationService.getSettings(UserId.of(reader.subject)))

    @Operation(summary = "Send an email to the customer when an order is placed")
    @PostMapping("/order-confirmation-email/enable")
    fun enableOrderConfirmationEmail(@AuthenticationPrincipal editor: Jwt): JsonShopSettings {
        val editorId = UserId.of(editor.subject)
        return JsonShopSettings(shopSettingsAdministrationService.enableOrderConfirmationEmail(editorId))
    }

    @Operation(summary = "Stop sending an email to the customer when an order is placed")
    @PostMapping("/order-confirmation-email/disable")
    fun disableOrderConfirmationEmail(@AuthenticationPrincipal editor: Jwt): JsonShopSettings {
        val editorId = UserId.of(editor.subject)
        return JsonShopSettings(shopSettingsAdministrationService.disableOrderConfirmationEmail(editorId))
    }

    @Operation(summary = "Remind the customers by email of the items left in their cart")
    @PostMapping("/abandoned-cart-reminder/enable")
    fun enableAbandonedCartReminder(@AuthenticationPrincipal editor: Jwt): JsonShopSettings {
        val editorId = UserId.of(editor.subject)
        return JsonShopSettings(shopSettingsAdministrationService.enableAbandonedCartReminder(editorId))
    }

    @Operation(summary = "Stop reminding the customers by email of the items left in their cart")
    @PostMapping("/abandoned-cart-reminder/disable")
    fun disableAbandonedCartReminder(@AuthenticationPrincipal editor: Jwt): JsonShopSettings {
        val editorId = UserId.of(editor.subject)
        return JsonShopSettings(shopSettingsAdministrationService.disableAbandonedCartReminder(editorId))
    }

    @Operation(summary = "Change the time a cart is left before its customer is reminded (1 to 720 hours)")
    @PutMapping("/abandoned-cart-delay")
    fun changeAbandonedCartDelay(
        @AuthenticationPrincipal editor: Jwt,
        @RequestBody abandonedCartDelay: JsonAbandonedCartDelay
    ): JsonShopSettings {
        val editorId = UserId.of(editor.subject)
        val administration = shopSettingsAdministrationService
        val delay = abandonedCartDelay.toAbandonedCartDelay()
        val changedSettings = administration.changeAbandonedCartDelay(editorId, delay)
        return JsonShopSettings(changedSettings)
    }

    @Operation(summary = "Change the time a customer has to pay an order (1 to 1440 minutes); unpaid, it is cancelled")
    @PutMapping("/payment-delay")
    fun changePaymentDelay(
        @AuthenticationPrincipal editor: Jwt,
        @RequestBody paymentDelay: JsonPaymentDelay
    ): JsonShopSettings {
        val editorId = UserId.of(editor.subject)
        val administration = shopSettingsAdministrationService
        val changedSettings = administration.changePaymentDelay(editorId, paymentDelay.toPaymentDelay())
        return JsonShopSettings(changedSettings)
    }

    @Operation(summary = "Change the price of the items from which the standard shipping is free (0: always free)")
    @PutMapping("/free-shipping-threshold")
    fun changeFreeShippingThreshold(
        @AuthenticationPrincipal editor: Jwt,
        @RequestBody threshold: JsonFreeShippingThreshold
    ): JsonShopSettings {
        val editorId = UserId.of(editor.subject)
        val freeShippingThreshold = threshold.toFreeShippingThreshold()
        val administration = shopSettingsAdministrationService
        val changedSettings = administration.changeFreeShippingThreshold(editorId, freeShippingThreshold)
        return JsonShopSettings(changedSettings)
    }
}
