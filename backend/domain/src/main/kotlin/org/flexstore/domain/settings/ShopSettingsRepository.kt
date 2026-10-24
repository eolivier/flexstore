package org.flexstore.domain.settings

interface ShopSettingsRepository {

    fun load(): ShopSettings

    fun save(settings: ShopSettings): ShopSettings
}
