package org.flexstore.domain.settings

class ShopSettingsUseCaseFacade(private val shopSettingsRepository: ShopSettingsRepository,
                                private val updateShopSettingsUseCase: UpdateShopSettingsUseCase) {

    fun getSettings(): ShopSettings = shopSettingsRepository.load()

    fun enableOrderConfirmationEmail() = update(getSettings().withOrderConfirmationEmailEnabled())

    fun disableOrderConfirmationEmail() = update(getSettings().withOrderConfirmationEmailDisabled())

    // The settings are read back once saved
    private fun update(settings: ShopSettings): ShopSettings {
        updateShopSettingsUseCase.unfold(settings)
        return shopSettingsRepository.load()
    }
}
