package org.flexstore.domain.preference

import org.flexstore.domain.product.model.Language
import org.flexstore.domain.user.model.UserId

interface UserPreferenceRepository {

    // Unchosen when the customer never chose
    fun preferenceOf(customer: UserId): ColorModePreference

    // Replaces the display mode of the customer
    fun save(customer: UserId, colorMode: ColorMode)

    // Unchosen when the customer never chose
    fun languagePreferenceOf(customer: UserId): LanguagePreference

    // Replaces the preferred language of the customer
    fun saveLanguage(customer: UserId, language: Language)
}
