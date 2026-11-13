package org.flexstore.domain.preference

// Whether the guided tour of the shop is shown to a customer: they hide it with "Do not show again" at its end
// and show it again from their preferences
enum class GuidedTourDisplay { SHOWN, HIDDEN }

// What a signed-in customer chose for the guided tour: shown or hidden, or nothing yet (it is shown then)
sealed interface GuidedTourPreference {

    data class Chosen(val guidedTourDisplay: GuidedTourDisplay) : GuidedTourPreference

    data object Unchosen : GuidedTourPreference
}
