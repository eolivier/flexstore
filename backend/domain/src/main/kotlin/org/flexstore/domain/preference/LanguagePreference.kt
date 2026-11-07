package org.flexstore.domain.preference

import org.flexstore.domain.product.model.Language

// What a signed-in customer chose for the language of the shop: a language, or nothing yet (the page decides)
sealed interface LanguagePreference {

    data class Chosen(val language: Language) : LanguagePreference

    data object Unchosen : LanguagePreference
}
