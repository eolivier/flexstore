package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.preference.LanguagePreference
import org.flexstore.domain.product.model.Language

// The language the customer chose: EN or FR, NONE when they never chose
data class JsonLanguagePreference(@JsonProperty("language") val language: String) {
    companion object {
        private const val NONE = "NONE"

        fun of(languagePreference: LanguagePreference) = when (languagePreference) {
            is LanguagePreference.Chosen -> JsonLanguagePreference(languagePreference.language.name)
            LanguagePreference.Unchosen -> JsonLanguagePreference(NONE)
        }
    }
}

// The language chosen by the customer
data class JsonLanguageChoice @JsonCreator constructor(@JsonProperty("language") val language: Language)
