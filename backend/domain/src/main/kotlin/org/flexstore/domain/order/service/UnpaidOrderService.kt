package org.flexstore.domain.order.service

import org.flexstore.domain.actor.ActorResolutionService
import org.flexstore.domain.order.model.*
import org.flexstore.domain.order.usecase.*
import org.flexstore.domain.sharedkernel.InstantProvider
import org.flexstore.domain.order.*
import org.flexstore.domain.settings.usecase.ReadShopSettingsUseCase
import org.ucop.domain.isNotNominal

// Domain service, run by the system user: the orders not paid within the payment delay of the shop settings
// are cancelled, so that their
// reserved units can be sold again. Called regularly by the infrastructure
class UnpaidOrderService(
    private val actorResolutionService: ActorResolutionService,
    private val readShopSettingsUseCase: ReadShopSettingsUseCase,
    private val unpaidOrderUseCases: UnpaidOrderUseCases
) {

    // The orders cancelled; an order whose payment has just completed is left as it is
    fun cancelLateOrders(): Orders {
        val paymentDelay = readShopSettingsUseCase.unfold(actorResolutionService.system()).paymentDelay
        val timeLimit = PaymentTimeLimit(paymentDelay, InstantProvider.now())
        val lateOrders = unpaidOrderUseCases.allOrders().latePaymentsAt(timeLimit).asList()
        val cancelledOrders = lateOrders.filter { order ->
            runCatching { unpaidOrderUseCases.cancel(actorResolutionService.system(), order, timeLimit) }
                .onFailure { failure -> if (failure.stopsTheRun()) throw failure }
                .isSuccess
        }
        return Orders(cancelledOrders)
    }

    // An order that cannot be cancelled is left aside; a failure that is not nominal, or the refusal of the
    // system itself, stops the run
    private fun Throwable.stopsTheRun() = isNotNominal() || cause is UnpaidOrderForbiddenException
}
