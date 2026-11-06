package org.flexstore.infra.repository

import org.flexstore.domain.preference.ColorMode
import org.flexstore.domain.preference.ColorModePreference
import org.flexstore.domain.preference.LanguagePreference
import org.flexstore.domain.preference.UserPreferenceRepository
import org.flexstore.domain.product.model.Language
import org.flexstore.domain.user.model.UserId

class InMemoryUserPreferenceRepository : UserPreferenceRepository {

    private val colorModes = mutableMapOf<UserId, ColorMode>()
    private val languages = mutableMapOf<UserId, Language>()

    override fun preferenceOf(customer: UserId): ColorModePreference =
        colorModes[customer]?.let { ColorModePreference.Chosen(it) } ?: ColorModePreference.Unchosen

    override fun save(customer: UserId, colorMode: ColorMode) {
        colorModes[customer] = colorMode
    }

    override fun languagePreferenceOf(customer: UserId): LanguagePreference =
        languages[customer]?.let { LanguagePreference.Chosen(it) } ?: LanguagePreference.Unchosen

    override fun saveLanguage(customer: UserId, language: Language) {
        languages[customer] = language
    }
}
