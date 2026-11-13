package org.flexstore.infra.spring.adapter.rest.json

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import org.flexstore.domain.preference.GuidedTourDisplay
import org.flexstore.domain.preference.GuidedTourPreference

// Whether the guided tour is shown to the customer: SHOWN or HIDDEN, NONE when they never chose
data class JsonGuidedTourPreference(@JsonProperty("guidedTour") val guidedTour: String) {
    companion object {
        private const val NONE = "NONE"

        fun of(guidedTourPreference: GuidedTourPreference) = when (guidedTourPreference) {
            is GuidedTourPreference.Chosen ->
                JsonGuidedTourPreference(guidedTourPreference.guidedTourDisplay.name)
            GuidedTourPreference.Unchosen -> JsonGuidedTourPreference(NONE)
        }
    }
}

// The display of the guided tour chosen by the customer
data class JsonGuidedTourChoice @JsonCreator constructor(
    @JsonProperty("guidedTour") val guidedTour: GuidedTourDisplay
)
