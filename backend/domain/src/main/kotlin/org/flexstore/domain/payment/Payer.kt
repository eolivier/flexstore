package org.flexstore.domain.payment

import org.flexstore.domain.order.Order
import org.flexstore.domain.user.model.*

// The holder of the card, when known: the payment page is prefilled with their name
sealed interface Cardholder {
    data class Named(val fullName: FullName) : Cardholder

    // A company that gave no person for the order: the customer enters the name on the payment page
    data object Unknown : Cardholder
}

// Who pays an order: the payment page is prefilled with their email and the name of the cardholder
data class Payer(val email: Email, val cardholder: Cardholder) {

    companion object {
        // A person pays with their own card: the signed-in customer. A company pays with the card of the person
        // given for the order (on its billing or delivery address), if any
        fun of(customer: User.DefinedUser, order: Order): Payer {
            val cardholder = when (order.addresses.billing.recipient) {
                is PrivateIndividual -> Cardholder.Named(customer.fullName)
                is LegalEntity -> companyContactOf(order)
            }
            return Payer(customer.email, cardholder)
        }

        private fun companyContactOf(order: Order): Cardholder {
            val addresses = listOf(order.addresses.billing, order.addresses.delivery)
            val contact = addresses.map { it.recipient }.filterIsInstance<LegalEntity>()
                .map { it.contact }
                .firstOrNull { it.isComplete() }
            return contact?.let { Cardholder.Named(it) } ?: Cardholder.Unknown
        }
    }
}

// The payment of an order to start, by its payer
data class PaymentRequest(val order: Order, val payer: Payer)
