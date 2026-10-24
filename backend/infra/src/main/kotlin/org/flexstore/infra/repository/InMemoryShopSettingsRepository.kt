package org.flexstore.infra.repository

import org.flexstore.domain.settings.*

class InMemoryShopSettingsRepository(private var settings: ShopSettings = ShopSettings.byDefault()) :
    ShopSettingsRepository {

    override fun load(): ShopSettings = settings

    override fun save(settings: ShopSettings): ShopSettings {
        this.settings = settings
        return settings
    }
}
