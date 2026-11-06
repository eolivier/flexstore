package org.flexstore.infra.spring.adapter.rest

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.flexstore.domain.preference.usecase.UserPreferenceUseCases
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.of
import org.flexstore.infra.spring.adapter.rest.json.JsonColorModeChoice
import org.flexstore.infra.spring.adapter.rest.json.JsonColorModePreference
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.*

// The display mode of the signed-in customer, kept in their account (the visitors keep theirs in their browser)
@Tag(name = "Preferences")
@RestController
class ColorModePreferenceController(private val userPreferenceUseCases: UserPreferenceUseCases) {

    @Operation(summary = "Retrieve the display mode of the signed-in customer (NONE when they never chose)")
    @GetMapping("/api/color-mode")
    fun getColorModePreference(@AuthenticationPrincipal customer: Jwt): JsonColorModePreference =
        JsonColorModePreference.of(userPreferenceUseCases.preferenceOf(UserId.of(customer.subject)))

    @Operation(summary = "Record the display mode chosen by the signed-in customer")
    @PutMapping("/api/color-mode")
    fun recordColorModePreference(
        @AuthenticationPrincipal customer: Jwt,
        @RequestBody colorModeChoice: JsonColorModeChoice
    ): JsonColorModePreference {
        val colorModePreference =
            userPreferenceUseCases.record(UserId.of(customer.subject), colorModeChoice.colorMode)
        return JsonColorModePreference.of(colorModePreference)
    }
}
