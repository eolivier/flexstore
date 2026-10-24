package org.flexstore.domain.settings

import org.ucop.domain.UseCaseLog

class ReadShopSettingsUseCase(private val shopSettingsRepository: ShopSettingsRepository) {

    fun getSettings(): ShopSettings = UseCaseLog.execution("ReadShopSettingsUseCase.getSettings") { shopSettingsRepository.load() }
}
