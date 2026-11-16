package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.cookie.usecase.CookieConsentUseCases
import org.flexstore.infra.spring.adapter.rest.json.JsonCookieChoice
import org.flexstore.infra.spring.adapter.rest.json.JsonCookieConsent
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

// The cookie consent of the signed-in customer, kept for 6 months (the visitors keep theirs in their browser)
@RestController
class CookieConsentController(
    private val cookieConsentUseCases: CookieConsentUseCases,
    private val actorResolutionService: ActorResolutionService
) {

    @GetMapping("/api/cookie-consent")
    fun getCookieConsent(@AuthenticationPrincipal customer: Jwt): JsonCookieConsent =
        JsonCookieConsent.of(cookieConsentUseCases.consentOf(actorResolutionService.actorOf(customer)))

    @PutMapping("/api/cookie-consent")
    fun recordCookieConsent(
        @AuthenticationPrincipal customer: Jwt,
        @RequestBody cookieChoice: JsonCookieChoice
    ): JsonCookieConsent {
        val actor = actorResolutionService.actorOf(customer)
        return JsonCookieConsent.of(cookieConsentUseCases.record(actor, cookieChoice.choice))
    }
}
