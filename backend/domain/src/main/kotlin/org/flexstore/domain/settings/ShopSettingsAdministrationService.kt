package org.flexstore.domain.settings

import org.flexstore.domain.settings.usecase.*
import org.ucop.domain.entity.Actor

// Domain service: the administrators turn the features of the shop on or off. A change reads the current
// settings, lets them apply it, saves them, then reads them back, all through the use cases
class ShopSettingsAdministrationService(
    private val readAdministeredShopSettingsUseCase: ReadAdministeredShopSettingsUseCase,
    private val updateShopSettingsUseCase: UpdateShopSettingsUseCase
) {

    fun getSettings(reader: Actor): ShopSettings =
        readAdministeredShopSettingsUseCase.unfold(ShopSettingsReading(reader))

    fun enableOrderConfirmationEmail(editor: Actor) =
        update(editor, getSettings(editor).withOrderConfirmationEmailEnabled())

    fun disableOrderConfirmationEmail(editor: Actor) =
        update(editor, getSettings(editor).withOrderConfirmationEmailDisabled())

    fun enableAbandonedCartReminder(editor: Actor) =
        update(editor, getSettings(editor).withAbandonedCartReminderEnabled())

    fun disableAbandonedCartReminder(editor: Actor) =
        update(editor, getSettings(editor).withAbandonedCartReminderDisabled())

    fun changeAbandonedCartDelay(editor: Actor, abandonedCartDelay: AbandonedCartDelay) =
        update(editor, getSettings(editor).withAbandonedCartDelay(abandonedCartDelay))

    fun changePaymentDelay(editor: Actor, paymentDelay: PaymentDelay) =
        update(editor, getSettings(editor).withPaymentDelay(paymentDelay))

    fun changeFreeShippingThreshold(editor: Actor, freeShippingThreshold: FreeShippingThreshold) =
        update(editor, getSettings(editor).withFreeShippingThreshold(freeShippingThreshold))

    // The seller setup is turned on by the setup assistant only, once finished: it is not a choice of the
    // administrators
    fun completeSellerSetup(editor: Actor) = update(editor, getSettings(editor).withSellerSetUp())

    private fun update(editor: Actor, settings: ShopSettings): ShopSettings {
        updateShopSettingsUseCase.unfold(ShopSettingsUpdate(editor, settings))
        return getSettings(editor)
    }
}
