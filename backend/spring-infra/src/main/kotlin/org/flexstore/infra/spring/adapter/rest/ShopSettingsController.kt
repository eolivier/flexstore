package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.settings.*
import org.flexstore.infra.spring.adapter.rest.json.JsonAbandonedCartDelay
import org.flexstore.infra.spring.adapter.rest.json.JsonFreeShippingThreshold
import org.flexstore.infra.spring.adapter.rest.json.JsonPaymentDelay
import org.flexstore.infra.spring.adapter.rest.json.JsonShopSettings
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

// The settings of the shop, for the administrators (ADMIN role, see SecurityConfig, checked again by the use
// cases)
@RestController
@RequestMapping("/api/admin/settings")
class ShopSettingsController(
    private val shopSettingsAdministrationService: ShopSettingsAdministrationService,
    private val actorResolutionService: ActorResolutionService
) {

    @GetMapping
    fun getSettings(@AuthenticationPrincipal reader: Jwt): JsonShopSettings {
        val actor = actorResolutionService.actorOf(reader)
        return JsonShopSettings(shopSettingsAdministrationService.getSettings(actor))
    }

    @PostMapping("/order-confirmation-email/enable")
    fun enableOrderConfirmationEmail(@AuthenticationPrincipal editor: Jwt): JsonShopSettings {
        val actor = actorResolutionService.actorOf(editor)
        return JsonShopSettings(shopSettingsAdministrationService.enableOrderConfirmationEmail(actor))
    }

    @PostMapping("/order-confirmation-email/disable")
    fun disableOrderConfirmationEmail(@AuthenticationPrincipal editor: Jwt): JsonShopSettings {
        val actor = actorResolutionService.actorOf(editor)
        return JsonShopSettings(shopSettingsAdministrationService.disableOrderConfirmationEmail(actor))
    }

    @PostMapping("/abandoned-cart-reminder/enable")
    fun enableAbandonedCartReminder(@AuthenticationPrincipal editor: Jwt): JsonShopSettings {
        val actor = actorResolutionService.actorOf(editor)
        return JsonShopSettings(shopSettingsAdministrationService.enableAbandonedCartReminder(actor))
    }

    @PostMapping("/abandoned-cart-reminder/disable")
    fun disableAbandonedCartReminder(@AuthenticationPrincipal editor: Jwt): JsonShopSettings {
        val actor = actorResolutionService.actorOf(editor)
        return JsonShopSettings(shopSettingsAdministrationService.disableAbandonedCartReminder(actor))
    }

    @PutMapping("/abandoned-cart-delay")
    fun changeAbandonedCartDelay(
        @AuthenticationPrincipal editor: Jwt,
        @RequestBody abandonedCartDelay: JsonAbandonedCartDelay
    ): JsonShopSettings {
        val actor = actorResolutionService.actorOf(editor)
        val administration = shopSettingsAdministrationService
        val delay = abandonedCartDelay.toAbandonedCartDelay()
        val changedSettings = administration.changeAbandonedCartDelay(actor, delay)
        return JsonShopSettings(changedSettings)
    }

    @PutMapping("/payment-delay")
    fun changePaymentDelay(
        @AuthenticationPrincipal editor: Jwt,
        @RequestBody paymentDelay: JsonPaymentDelay
    ): JsonShopSettings {
        val actor = actorResolutionService.actorOf(editor)
        val administration = shopSettingsAdministrationService
        val changedSettings = administration.changePaymentDelay(actor, paymentDelay.toPaymentDelay())
        return JsonShopSettings(changedSettings)
    }

    @PutMapping("/free-shipping-threshold")
    fun changeFreeShippingThreshold(
        @AuthenticationPrincipal editor: Jwt,
        @RequestBody threshold: JsonFreeShippingThreshold
    ): JsonShopSettings {
        val actor = actorResolutionService.actorOf(editor)
        val freeShippingThreshold = threshold.toFreeShippingThreshold()
        val administration = shopSettingsAdministrationService
        val changedSettings = administration.changeFreeShippingThreshold(actor, freeShippingThreshold)
        return JsonShopSettings(changedSettings)
    }
}
