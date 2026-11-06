package org.flexstore.domain.preference.usecase

import org.flexstore.domain.preference.*
import org.flexstore.domain.user.model.UserId

// The use cases of the preferences of a signed-in customer (their display mode for now):
// a preference is read back once recorded
class UserPreferenceUseCases(
    private val readUserPreferenceUseCase: ReadUserPreferenceUseCase,
    private val recordUserPreferenceUseCase: RecordUserPreferenceUseCase
) {
    fun preferenceOf(customer: UserId): ColorModePreference =
        readUserPreferenceUseCase.unfold(ColorModePreferenceReading(customer))

    fun record(customer: UserId, colorMode: ColorMode): ColorModePreference {
        recordUserPreferenceUseCase.unfold(ColorModeChoice(customer, colorMode))
        return preferenceOf(customer)
    }
}
