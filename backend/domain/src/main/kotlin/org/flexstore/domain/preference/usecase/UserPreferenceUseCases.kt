package org.flexstore.domain.preference.usecase

import org.flexstore.domain.preference.*
import org.ucop.domain.entity.Actor

// The use cases of the preferences of a signed-in customer (their display mode, their language and their guided
// tour): the preferences are read back once one is recorded
class UserPreferenceUseCases(
    private val readUserPreferenceUseCase: ReadUserPreferenceUseCase,
    private val recordUserPreferenceUseCase: RecordUserPreferenceUseCase
) {
    fun preferencesOf(customer: Actor): UserPreferences =
        readUserPreferenceUseCase.unfold(UserPreferencesReading(customer))

    fun record(customer: Actor, preference: UserPreference): UserPreferences {
        recordUserPreferenceUseCase.unfold(UserPreferenceChoice(customer, preference))
        return preferencesOf(customer)
    }
}
