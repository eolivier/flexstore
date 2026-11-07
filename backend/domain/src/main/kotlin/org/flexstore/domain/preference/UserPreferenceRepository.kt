package org.flexstore.domain.preference

import org.flexstore.domain.user.model.UserId

interface UserPreferenceRepository {

    // Each preference is unchosen when the customer never chose it
    fun preferencesOf(customer: UserId): UserPreferences

    // Replaces the stored preference of the same kind, the other one is kept
    fun save(customer: UserId, preference: UserPreference)
}
