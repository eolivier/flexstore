package org.flexstore.infra.repository

import org.flexstore.domain.preference.ColorMode
import org.flexstore.domain.preference.ColorModePreference
import org.flexstore.domain.preference.ColorModePreferenceRepository
import org.flexstore.domain.user.model.UserId

class InMemoryColorModePreferenceRepository : ColorModePreferenceRepository {

    private val colorModes = mutableMapOf<UserId, ColorMode>()

    override fun preferenceOf(customer: UserId): ColorModePreference =
        colorModes[customer]?.let { ColorModePreference.Chosen(it) } ?: ColorModePreference.Unchosen

    override fun save(customer: UserId, colorMode: ColorMode) {
        colorModes[customer] = colorMode
    }
}
