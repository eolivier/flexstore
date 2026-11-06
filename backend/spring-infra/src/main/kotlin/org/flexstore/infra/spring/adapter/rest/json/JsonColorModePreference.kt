package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.preference.ColorMode
import org.flexstore.domain.preference.ColorModePreference

// The display mode the customer chose: LIGHT or DARK, NONE when they never chose
data class JsonColorModePreference(@JsonProperty("colorMode") val colorMode: String) {
    companion object {
        private const val NONE = "NONE"

        fun of(colorModePreference: ColorModePreference) = when (colorModePreference) {
            is ColorModePreference.Chosen -> JsonColorModePreference(colorModePreference.colorMode.name)
            ColorModePreference.Unchosen -> JsonColorModePreference(NONE)
        }
    }
}

// The display mode chosen by the customer
data class JsonColorModeChoice @JsonCreator constructor(@JsonProperty("colorMode") val colorMode: ColorMode)
