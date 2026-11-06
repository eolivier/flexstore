package org.flexstore.domain.preference.usecase

import org.flexstore.domain.preference.*
import org.flexstore.domain.user.model.UserId

// The use cases of the display mode of a signed-in customer: it is read back once recorded
class ColorModePreferenceUseCases(
    private val readColorModePreferenceUseCase: ReadColorModePreferenceUseCase,
    private val recordColorModePreferenceUseCase: RecordColorModePreferenceUseCase
) {
    fun preferenceOf(customer: UserId): ColorModePreference =
        readColorModePreferenceUseCase.unfold(ColorModePreferenceReading(customer))

    fun record(customer: UserId, colorMode: ColorMode): ColorModePreference {
        recordColorModePreferenceUseCase.unfold(ColorModeChoice(customer, colorMode))
        return preferenceOf(customer)
    }
}
