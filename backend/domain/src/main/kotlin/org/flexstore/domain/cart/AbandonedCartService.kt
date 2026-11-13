package org.flexstore.domain.cart

import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.cart.usecase.*
import org.flexstore.domain.settings.usecase.ReadShopSettingsUseCase
import org.flexstore.domain.sharedkernel.InstantProvider
import org.ucop.domain.isNotNominal

// Domain service, run by the system user: when the shop settings turn the reminder on, the customers who left
// their cart for the delay
// of the settings are reminded of it by email. Called regularly by the infrastructure
class AbandonedCartService(
    private val actorResolutionService: ActorResolutionService,
    private val readShopSettingsUseCase: ReadShopSettingsUseCase,
    private val abandonedCartUseCases: AbandonedCartUseCases
) {

    // The carts whose customer was reminded; a customer who cannot be reminded is left aside
    fun remindAbandonedCarts(): AbandonedCarts {
        val system = actorResolutionService.system()
        val shopSettings = readShopSettingsUseCase.unfold(system)
        if (shopSettings.sendsAbandonedCartReminder()) {
            val limit = AbandonmentLimit.of(shopSettings.abandonedCartDelay, InstantProvider.now())
            val abandonedCarts = abandonedCartUseCases.leftSince(system, limit).asList()
            return AbandonedCarts(abandonedCarts.filter { cart -> isReminded(cart) })
        }
        return AbandonedCarts()
    }

    private fun isReminded(cart: Cart): Boolean {
        val customer = cart.owner as? CartOwner.Customer ?: return false
        return runCatching { abandonedCartUseCases.remind(actorResolutionService.system(), customer) }
            .onFailure { failure -> if (failure.stopsTheRun()) throw failure }
            .isSuccess
    }

    // A customer who cannot be reminded is left aside; a failure that is not nominal, or the refusal of the
    // system itself, stops the run
    private fun Throwable.stopsTheRun() = isNotNominal() || cause is AbandonedCartForbiddenException
}
