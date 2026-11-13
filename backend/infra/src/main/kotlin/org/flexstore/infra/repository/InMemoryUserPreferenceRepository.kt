package org.flexstore.infra.repository

import org.flexstore.domain.preference.*
import org.flexstore.domain.product.model.Language
import org.flexstore.domain.user.model.UserId

class InMemoryUserPreferenceRepository : UserPreferenceRepository {

    private val colorModes = mutableMapOf<UserId, ColorMode>()
    private val languages = mutableMapOf<UserId, Language>()
    private val guidedTours = mutableMapOf<UserId, GuidedTourDisplay>()

    override fun preferencesOf(customer: UserId) = UserPreferences(
        colorModes[customer]?.let { ColorModePreference.Chosen(it) } ?: ColorModePreference.Unchosen,
        languages[customer]?.let { LanguagePreference.Chosen(it) } ?: LanguagePreference.Unchosen,
        guidedTours[customer]?.let { GuidedTourPreference.Chosen(it) } ?: GuidedTourPreference.Unchosen
    )

    override fun save(customer: UserId, preference: UserPreference) {
        when (preference) {
            is UserPreference.DisplayMode -> colorModes[customer] = preference.colorMode
            is UserPreference.PreferredLanguage -> languages[customer] = preference.language
            is UserPreference.GuidedTour -> guidedTours[customer] = preference.guidedTourDisplay
        }
    }
}
