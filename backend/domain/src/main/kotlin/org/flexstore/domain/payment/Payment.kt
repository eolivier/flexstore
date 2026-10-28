package org.flexstore.domain.payment

import org.flexstore.domain.cart.CartOwner
import org.flexstore.domain.order.OrderId
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.ucop.domain.NominalException

// The page of the payment provider where the customer pays an order
data class CheckoutPage(val url: String)

// PENDING: the customer pays on the checkout page, the provider confirms it later;
// PAID: the provider confirmed the payment at once (a simulated payment)
enum class PaymentOutcome { PENDING, PAID }

// A payment started on the payment provider for an order
data class PaymentSession(val checkoutPage: CheckoutPage, val outcome: PaymentOutcome) {
    fun isPaid() = outcome == PaymentOutcome.PAID
}

// A customer pays one of their orders
data class OrderPayment(val customer: CartOwner.Customer, val orderId: OrderId)

data class OrderAlreadyPaidException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
// The customer paid, but the payment provider has not confirmed it yet
data class PaymentAlreadyCompletedException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
// The payment provider refused to give the payment back (the payment is not refundable, the provider is not
// reachable...)
data class PaymentRefundRefusedException(override val errorMessage: ErrorMessage) :
    NominalException(errorMessage)
data class PaymentNotStartedException(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
data class PaymentConfirmationFailed(override val errorMessage: ErrorMessage) : NominalException(errorMessage)
