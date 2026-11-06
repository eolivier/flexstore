package org.flexstore.domain.preference

import org.flexstore.domain.user.model.UserId
import org.ucop.domain.ErrorMessage
import org.ucop.domain.NominalException

// The display mode of the shop
enum class ColorMode { LIGHT, DARK }

// What a signed-in customer chose for the display mode: a mode, or nothing yet (the browser decides then)
sealed interface ColorModePreference {

    data class Chosen(val colorMode: ColorMode) : ColorModePreference

    data object Unchosen : ColorModePreference
}

// A customer chooses their display mode
data class ColorModeChoice(val customer: UserId, val colorMode: ColorMode)

// A customer's display mode is read
data class ColorModePreferenceReading(val customer: UserId)

data class ColorModePreferenceRecordFailed(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
