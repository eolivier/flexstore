package org.flexstore.domain.preference.usecase

import org.flexstore.domain.preference.*
import org.flexstore.domain.user.model.UserId

// The use cases of the preferences of a signed-in customer (their display mode and their language):
// the preferences are read back once one is recorded
class UserPreferenceUseCases(
    private val readUserPreferenceUseCase: ReadUserPreferenceUseCase,
    private val recordUserPreferenceUseCase: RecordUserPreferenceUseCase
) {
    fun preferencesOf(customer: UserId): UserPreferences =
        readUserPreferenceUseCase.unfold(UserPreferencesReading(customer))

    fun record(customer: UserId, preference: UserPreference): UserPreferences {
        recordUserPreferenceUseCase.unfold(UserPreferenceChoice(customer, preference))
        return preferencesOf(customer)
    }
}
