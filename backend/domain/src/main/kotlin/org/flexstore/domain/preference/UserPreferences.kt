package org.flexstore.domain.preference

import org.flexstore.domain.product.model.Language
import org.flexstore.domain.user.model.UserId
import org.ucop.domain.ErrorMessage
import org.ucop.domain.PostConditionException

// What a signed-in customer chose for the shop so far: their display mode, their language and whether the
// guided tour is shown to them
data class UserPreferences(
    val colorMode: ColorModePreference,
    val language: LanguagePreference,
    val guidedTour: GuidedTourPreference
) {

    fun contain(preference: UserPreference) = when (preference) {
        is UserPreference.DisplayMode -> colorMode == ColorModePreference.Chosen(preference.colorMode)
        is UserPreference.PreferredLanguage -> language == LanguagePreference.Chosen(preference.language)
        is UserPreference.GuidedTour ->
            guidedTour == GuidedTourPreference.Chosen(preference.guidedTourDisplay)
    }

    fun doNotContain(preference: UserPreference) = !contain(preference)
}

// One preference a customer chooses
sealed interface UserPreference {

    data class DisplayMode(val colorMode: ColorMode) : UserPreference

    data class PreferredLanguage(val language: Language) : UserPreference

    data class GuidedTour(val guidedTourDisplay: GuidedTourDisplay) : UserPreference
}

// A customer chooses a preference
data class UserPreferenceChoice(val customer: UserId, val preference: UserPreference)

// A customer's preferences are read
data class UserPreferencesReading(val customer: UserId)

data class UserPreferenceRecordFailed(override val errorMessage: ErrorMessage) :
    PostConditionException(errorMessage)
