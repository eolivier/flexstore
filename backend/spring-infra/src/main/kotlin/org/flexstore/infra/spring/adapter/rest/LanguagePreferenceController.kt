package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.preference.UserPreference
import org.flexstore.domain.preference.usecase.UserPreferenceUseCases
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.of
import org.flexstore.infra.spring.adapter.rest.json.JsonLanguageChoice
import org.flexstore.infra.spring.adapter.rest.json.JsonLanguagePreference
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

// The preferred language of the signed-in customer, kept in their account
@Tag(name = "Preferences")
@RestController
class LanguagePreferenceController(private val userPreferenceUseCases: UserPreferenceUseCases) {

    @Operation(
        summary = "Retrieve the preferred language of the signed-in customer (NONE when they never chose)"
    )
    @GetMapping("/api/language")
    fun getLanguagePreference(@AuthenticationPrincipal customer: Jwt): JsonLanguagePreference =
        JsonLanguagePreference.of(userPreferenceUseCases.preferencesOf(UserId.of(customer.subject)).language)

    @Operation(summary = "Record the preferred language chosen by the signed-in customer")
    @PutMapping("/api/language")
    fun recordLanguagePreference(
        @AuthenticationPrincipal customer: Jwt,
        @RequestBody languageChoice: JsonLanguageChoice
    ): JsonLanguagePreference {
        val preferredLanguage = UserPreference.PreferredLanguage(languageChoice.language)
        val preferences = userPreferenceUseCases.record(UserId.of(customer.subject), preferredLanguage)
        return JsonLanguagePreference.of(preferences.language)
    }
}
