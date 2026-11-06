package org.flexstore.domain.preference

import org.flexstore.domain.product.model.Language
import org.flexstore.domain.user.model.UserId
import org.ucop.domain.ErrorMessage
import org.ucop.domain.NominalException

// What a signed-in customer chose for the language of the shop: a language, or nothing yet (the page decides)
sealed interface LanguagePreference {

    data class Chosen(val language: Language) : LanguagePreference

    data object Unchosen : LanguagePreference
}

// A customer chooses their preferred language
data class LanguageChoice(val customer: UserId, val language: Language)

// A customer's preferred language is read
data class LanguagePreferenceReading(val customer: UserId)

data class LanguagePreferenceRecordFailed(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
