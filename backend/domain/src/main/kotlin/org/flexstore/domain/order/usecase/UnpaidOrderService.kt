package org.flexstore.domain.order.usecase

import org.flexstore.domain.order.*
import org.flexstore.domain.settings.ReadShopSettingsUseCase
import org.ucop.domain.NominalException
import java.time.Clock

// Domain service: the orders not paid within the payment delay of the shop settings are cancelled, so that their
// reserved units can be sold again. Called regularly by the infrastructure
class UnpaidOrderService(
    private val readShopSettingsUseCase: ReadShopSettingsUseCase,
    private val readOrdersUseCase: ReadOrdersUseCase,
    private val cancelUnpaidOrderUseCase: CancelUnpaidOrderUseCase,
    private val clock: Clock = Clock.systemUTC()
) {

    // The orders cancelled; an order whose payment has just completed is left as it is
    fun cancelLateOrders(): Orders {
        val paymentDelay = readShopSettingsUseCase.getSettings().paymentDelay
        val timeLimit = PaymentTimeLimit(paymentDelay, clock.instant())
        val lateOrders = readOrdersUseCase.getAllOrders().latePaymentsAt(timeLimit).asList()
        val cancelledOrders = lateOrders.filter { order ->
            val cancellation = UnpaidOrderCancellation(order.orderId, timeLimit)
            runCatching { cancelUnpaidOrderUseCase.unfold(cancellation) }
                .onFailure { if (it !is NominalException) throw it }
                .isSuccess
        }
        return Orders(cancelledOrders)
    }
}
