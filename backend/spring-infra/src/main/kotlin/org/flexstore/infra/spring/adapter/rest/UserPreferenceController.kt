package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.preference.UserPreference
import org.flexstore.domain.preference.UserPreferences
import org.flexstore.domain.preference.usecase.UserPreferenceUseCases
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.of
import org.flexstore.infra.spring.adapter.rest.json.JsonColorModeChoice
import org.flexstore.infra.spring.adapter.rest.json.JsonColorModePreference
import org.flexstore.infra.spring.adapter.rest.json.JsonGuidedTourChoice
import org.flexstore.infra.spring.adapter.rest.json.JsonGuidedTourPreference
import org.flexstore.infra.spring.adapter.rest.json.JsonLanguageChoice
import org.flexstore.infra.spring.adapter.rest.json.JsonLanguagePreference
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

// The preferences of the signed-in customer, kept in their account (the visitors keep their display mode in
// their browser): display mode, guided tour and preferred language
@Tag(name = "Preferences")
@RestController
class UserPreferenceController(private val userPreferenceUseCases: UserPreferenceUseCases) {

    @Operation(summary = "Retrieve the display mode of the signed-in customer (NONE when they never chose)")
    @GetMapping("/api/color-mode")
    fun getColorModePreference(@AuthenticationPrincipal customer: Jwt): JsonColorModePreference =
        JsonColorModePreference.of(preferencesOf(customer).colorMode)

    @Operation(summary = "Record the display mode chosen by the signed-in customer")
    @PutMapping("/api/color-mode")
    fun recordColorModePreference(
        @AuthenticationPrincipal customer: Jwt,
        @RequestBody colorModeChoice: JsonColorModeChoice
    ): JsonColorModePreference {
        val displayMode = UserPreference.DisplayMode(colorModeChoice.colorMode)
        return JsonColorModePreference.of(record(customer, displayMode).colorMode)
    }

    @Operation(
        summary = "Retrieve whether the guided tour is shown to the signed-in customer (NONE if never chosen)"
    )
    @GetMapping("/api/guided-tour")
    fun getGuidedTourPreference(@AuthenticationPrincipal customer: Jwt): JsonGuidedTourPreference =
        JsonGuidedTourPreference.of(preferencesOf(customer).guidedTour)

    @Operation(summary = "Record whether the guided tour is shown to the signed-in customer")
    @PutMapping("/api/guided-tour")
    fun recordGuidedTourPreference(
        @AuthenticationPrincipal customer: Jwt,
        @RequestBody guidedTourChoice: JsonGuidedTourChoice
    ): JsonGuidedTourPreference {
        val guidedTour = UserPreference.GuidedTour(guidedTourChoice.guidedTour)
        return JsonGuidedTourPreference.of(record(customer, guidedTour).guidedTour)
    }

    @Operation(
        summary = "Retrieve the preferred language of the signed-in customer (NONE when they never chose)"
    )
    @GetMapping("/api/language")
    fun getLanguagePreference(@AuthenticationPrincipal customer: Jwt): JsonLanguagePreference =
        JsonLanguagePreference.of(preferencesOf(customer).language)

    @Operation(summary = "Record the preferred language chosen by the signed-in customer")
    @PutMapping("/api/language")
    fun recordLanguagePreference(
        @AuthenticationPrincipal customer: Jwt,
        @RequestBody languageChoice: JsonLanguageChoice
    ): JsonLanguagePreference {
        val preferredLanguage = UserPreference.PreferredLanguage(languageChoice.language)
        return JsonLanguagePreference.of(record(customer, preferredLanguage).language)
    }

    private fun preferencesOf(customer: Jwt): UserPreferences =
        userPreferenceUseCases.preferencesOf(UserId.of(customer.subject))

    private fun record(customer: Jwt, userPreference: UserPreference): UserPreferences =
        userPreferenceUseCases.record(UserId.of(customer.subject), userPreference)
}
