package org.flexstore.infra.spring.adapter.rest

import org.flexstore.domain.preference.UserPreference
import org.flexstore.domain.preference.UserPreferences
import org.flexstore.domain.preference.usecase.UserPreferenceUseCases
import org.flexstore.domain.actor.ActorResolutionService
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
@RestController
class UserPreferenceController(
    private val userPreferenceUseCases: UserPreferenceUseCases,
    private val actorResolutionService: ActorResolutionService
) {

    @GetMapping("/api/color-mode")
    fun getColorModePreference(@AuthenticationPrincipal customer: Jwt): JsonColorModePreference =
        JsonColorModePreference.of(preferencesOf(customer).colorMode)

    @PutMapping("/api/color-mode")
    fun recordColorModePreference(
        @AuthenticationPrincipal customer: Jwt,
        @RequestBody colorModeChoice: JsonColorModeChoice
    ): JsonColorModePreference {
        val displayMode = UserPreference.DisplayMode(colorModeChoice.colorMode)
        return JsonColorModePreference.of(record(customer, displayMode).colorMode)
    }

    @GetMapping("/api/guided-tour")
    fun getGuidedTourPreference(@AuthenticationPrincipal customer: Jwt): JsonGuidedTourPreference =
        JsonGuidedTourPreference.of(preferencesOf(customer).guidedTour)

    @PutMapping("/api/guided-tour")
    fun recordGuidedTourPreference(
        @AuthenticationPrincipal customer: Jwt,
        @RequestBody guidedTourChoice: JsonGuidedTourChoice
    ): JsonGuidedTourPreference {
        val guidedTour = UserPreference.GuidedTour(guidedTourChoice.guidedTour)
        return JsonGuidedTourPreference.of(record(customer, guidedTour).guidedTour)
    }

    @GetMapping("/api/language")
    fun getLanguagePreference(@AuthenticationPrincipal customer: Jwt): JsonLanguagePreference =
        JsonLanguagePreference.of(preferencesOf(customer).language)

    @PutMapping("/api/language")
    fun recordLanguagePreference(
        @AuthenticationPrincipal customer: Jwt,
        @RequestBody languageChoice: JsonLanguageChoice
    ): JsonLanguagePreference {
        val preferredLanguage = UserPreference.PreferredLanguage(languageChoice.language)
        return JsonLanguagePreference.of(record(customer, preferredLanguage).language)
    }

    private fun preferencesOf(customer: Jwt): UserPreferences =
        userPreferenceUseCases.preferencesOf(actorResolutionService.actorOf(customer))

    private fun record(customer: Jwt, userPreference: UserPreference): UserPreferences =
        userPreferenceUseCases.record(actorResolutionService.actorOf(customer), userPreference)
}
