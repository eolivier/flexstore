package org.flexstore.domain.settings

import org.flexstore.domain.settings.usecase.*
import org.flexstore.domain.user.model.UserId

// Domain service: the administrators turn the features of the shop on or off. A change reads the current
// settings, lets them apply it, saves them, then reads them back, all through the use cases
class ShopSettingsAdministrationService(
    private val readAdministeredShopSettingsUseCase: ReadAdministeredShopSettingsUseCase,
    private val updateShopSettingsUseCase: UpdateShopSettingsUseCase
) {

    fun getSettings(reader: UserId): ShopSettings =
        readAdministeredShopSettingsUseCase.unfold(ShopSettingsReading(reader))

    fun enableOrderConfirmationEmail(editor: UserId) =
        update(editor, getSettings(editor).withOrderConfirmationEmailEnabled())

    fun disableOrderConfirmationEmail(editor: UserId) =
        update(editor, getSettings(editor).withOrderConfirmationEmailDisabled())

    fun changePaymentDelay(editor: UserId, paymentDelay: PaymentDelay) =
        update(editor, getSettings(editor).withPaymentDelay(paymentDelay))

    fun changeFreeShippingThreshold(editor: UserId, freeShippingThreshold: FreeShippingThreshold) =
        update(editor, getSettings(editor).withFreeShippingThreshold(freeShippingThreshold))

    // The seller setup is turned on by the setup assistant only, once finished: it is not a choice of the
    // administrators
    fun completeSellerSetup(editor: UserId) = update(editor, getSettings(editor).withSellerSetUp())

    private fun update(editor: UserId, settings: ShopSettings): ShopSettings {
        updateShopSettingsUseCase.unfold(ShopSettingsUpdate(editor, settings))
        return getSettings(editor)
    }
}
