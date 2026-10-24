package org.flexstore.domain.order

import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.*

// The email that confirms an order to its customer; its wording is up to the infrastructure that sends it
data class OrderConfirmationEmail(val recipient: Email, val customerName: Name, val order: Order)
