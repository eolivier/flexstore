package org.flexstore.domain.settings

// Domain service: the administrators turn the features of the shop on or off. A change reads the current
// settings, lets them apply it, saves them, then reads them back, all through the use cases
class ShopSettingsAdministrationService(private val readShopSettingsUseCase: ReadShopSettingsUseCase,
                                 private val updateShopSettingsUseCase: UpdateShopSettingsUseCase) {

    fun getSettings(): ShopSettings = readShopSettingsUseCase.getSettings()

    fun enableOrderConfirmationEmail() = update(getSettings().withOrderConfirmationEmailEnabled())

    fun disableOrderConfirmationEmail() = update(getSettings().withOrderConfirmationEmailDisabled())

    fun changePaymentDelay(paymentDelay: PaymentDelay) = update(getSettings().withPaymentDelay(paymentDelay))

    private fun update(settings: ShopSettings): ShopSettings {
        updateShopSettingsUseCase.unfold(settings)
        return readShopSettingsUseCase.getSettings()
    }
}
