package org.flexstore.domain.preference.usecase

import org.flexstore.domain.preference.*
import org.flexstore.domain.product.model.Language
import org.flexstore.domain.user.model.UserId

// The use cases of the preferences of a signed-in customer (their display mode and their language):
// a preference is read back once recorded
class UserPreferenceUseCases(
    private val readUserPreferenceUseCase: ReadUserPreferenceUseCase,
    private val recordUserPreferenceUseCase: RecordUserPreferenceUseCase,
    private val readLanguagePreferenceUseCase: ReadLanguagePreferenceUseCase,
    private val recordLanguagePreferenceUseCase: RecordLanguagePreferenceUseCase
) {
    fun preferenceOf(customer: UserId): ColorModePreference =
        readUserPreferenceUseCase.unfold(ColorModePreferenceReading(customer))

    fun record(customer: UserId, colorMode: ColorMode): ColorModePreference {
        recordUserPreferenceUseCase.unfold(ColorModeChoice(customer, colorMode))
        return preferenceOf(customer)
    }

    fun languagePreferenceOf(customer: UserId): LanguagePreference =
        readLanguagePreferenceUseCase.unfold(LanguagePreferenceReading(customer))

    fun recordLanguage(customer: UserId, language: Language): LanguagePreference {
        recordLanguagePreferenceUseCase.unfold(LanguageChoice(customer, language))
        return languagePreferenceOf(customer)
    }
}
