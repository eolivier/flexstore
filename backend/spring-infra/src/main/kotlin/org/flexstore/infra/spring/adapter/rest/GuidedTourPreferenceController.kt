package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.preference.UserPreference
import org.flexstore.domain.preference.usecase.UserPreferenceUseCases
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.of
import org.flexstore.infra.spring.adapter.rest.json.JsonGuidedTourChoice
import org.flexstore.infra.spring.adapter.rest.json.JsonGuidedTourPreference
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

// Whether the guided tour of the shop is shown to the signed-in customer, kept in their account
@Tag(name = "Preferences")
@RestController
class GuidedTourPreferenceController(private val userPreferenceUseCases: UserPreferenceUseCases) {

    @Operation(
        summary = "Retrieve whether the guided tour is shown to the signed-in customer (NONE if never chosen)"
    )
    @GetMapping("/api/guided-tour")
    fun getGuidedTourPreference(@AuthenticationPrincipal customer: Jwt): JsonGuidedTourPreference =
        JsonGuidedTourPreference.of(userPreferenceUseCases.preferencesOf(UserId.of(customer.subject)).guidedTour)

    @Operation(summary = "Record whether the guided tour is shown to the signed-in customer")
    @PutMapping("/api/guided-tour")
    fun recordGuidedTourPreference(
        @AuthenticationPrincipal customer: Jwt,
        @RequestBody guidedTourChoice: JsonGuidedTourChoice
    ): JsonGuidedTourPreference {
        val guidedTour = UserPreference.GuidedTour(guidedTourChoice.guidedTour)
        val preferences = userPreferenceUseCases.record(UserId.of(customer.subject), guidedTour)
        return JsonGuidedTourPreference.of(preferences.guidedTour)
    }
}
