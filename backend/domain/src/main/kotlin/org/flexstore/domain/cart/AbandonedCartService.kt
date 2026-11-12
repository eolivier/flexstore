package org.flexstore.domain.cart

import org.flexstore.domain.actor.SystemActor
import org.flexstore.domain.cart.usecase.*
import org.flexstore.domain.settings.usecase.ReadShopSettingsUseCase
import org.flexstore.domain.sharedkernel.InstantProvider
import org.ucop.domain.NominalException

// Domain service: when the shop settings turn the reminder on, the customers who left their cart for the delay
// of the settings are reminded of it by email. Called regularly by the infrastructure
class AbandonedCartService(
    private val readShopSettingsUseCase: ReadShopSettingsUseCase,
    private val listAbandonedCartsUseCase: ListAbandonedCartsUseCase,
    private val remindAbandonedCartUseCase: RemindAbandonedCartUseCase
) {

    // The carts whose customer was reminded; a customer who cannot be reminded is left aside
    fun remindAbandonedCarts(): AbandonedCarts {
        val shopSettings = readShopSettingsUseCase.unfold(Unit)
        if (shopSettings.sendsAbandonedCartReminder()) {
            val limit = AbandonmentLimit.of(shopSettings.abandonedCartDelay, InstantProvider.now())
            val reading = AbandonedCartsReading(SystemActor(), limit)
            val abandonedCarts = listAbandonedCartsUseCase.unfold(reading).asList()
            return AbandonedCarts(abandonedCarts.filter { cart -> isReminded(cart) })
        }
        return AbandonedCarts()
    }

    private fun isReminded(cart: Cart): Boolean {
        val customer = cart.owner as? CartOwner.Customer ?: return false
        return runCatching { remindAbandonedCartUseCase.unfold(AbandonedCartReminding(SystemActor(), customer)) }
            .onFailure { if (it !is NominalException) throw it }
            .isSuccess
    }
}
