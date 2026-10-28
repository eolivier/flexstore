package org.flexstore.domain.email

import org.flexstore.domain.order.Order
import org.flexstore.domain.user.model.*

// The email that tells a customer that their order reached a new status (shipped, then delivered); its wording
// is OrderStatusEmailContent
data class OrderStatusEmail(val recipient: Email, val customerName: FullName, val order: Order)
