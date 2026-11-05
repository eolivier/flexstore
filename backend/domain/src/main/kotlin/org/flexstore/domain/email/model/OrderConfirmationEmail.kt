package org.flexstore.domain.email.model

import org.flexstore.domain.order.model.Order
import org.flexstore.domain.user.model.*

// The email that confirms an order to its customer; its wording is OrderConfirmationEmailContent
data class OrderConfirmationEmail(val recipient: Email, val customerName: FullName, val order: Order)
