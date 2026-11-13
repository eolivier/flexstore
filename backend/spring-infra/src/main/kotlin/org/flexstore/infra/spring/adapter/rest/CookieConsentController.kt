package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.cookie.usecase.CookieConsentUseCases
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.of
import org.flexstore.infra.spring.adapter.rest.json.JsonCookieChoice
import org.flexstore.infra.spring.adapter.rest.json.JsonCookieConsent
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

// The cookie consent of the signed-in customer, kept for 6 months (the visitors keep theirs in their browser)
@Tag(name = "Cookies")
@RestController
class CookieConsentController(private val cookieConsentUseCases: CookieConsentUseCases) {

    @Operation(summary = "Retrieve the cookie consent of the signed-in customer (UNDECIDED once it expired)")
    @GetMapping("/api/cookie-consent")
    fun getCookieConsent(@AuthenticationPrincipal customer: Jwt): JsonCookieConsent =
        JsonCookieConsent.of(cookieConsentUseCases.consentOf(UserId.of(customer.subject)))

    @Operation(
        summary = "Record the choice of the signed-in customer about the preference cookies, for 6 months"
    )
    @PutMapping("/api/cookie-consent")
    fun recordCookieConsent(
        @AuthenticationPrincipal customer: Jwt,
        @RequestBody cookieChoice: JsonCookieChoice
    ): JsonCookieConsent =
        JsonCookieConsent.of(cookieConsentUseCases.record(UserId.of(customer.subject), cookieChoice.choice))
}
