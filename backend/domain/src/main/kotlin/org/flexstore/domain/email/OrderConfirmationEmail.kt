package org.flexstore.domain.email

import org.flexstore.domain.order.Order
import org.flexstore.domain.user.*

// The email that confirms an order to its customer; its wording is up to the infrastructure that sends it
data class OrderConfirmationEmail(val recipient: Email, val customerName: FullName, val order: Order)
