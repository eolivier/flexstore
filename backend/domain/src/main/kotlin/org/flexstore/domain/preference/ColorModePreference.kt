package org.flexstore.domain.preference

// The display mode of the shop
enum class ColorMode { LIGHT, DARK }

// What a signed-in customer chose for the display mode: a mode, or nothing yet (the browser decides then)
sealed interface ColorModePreference {

    data class Chosen(val colorMode: ColorMode) : ColorModePreference

    data object Unchosen : ColorModePreference
}
