package org.flexstore.domain.email.model

import org.flexstore.domain.cart.Cart
import org.flexstore.domain.user.model.*

// The email that reminds a customer of the items left in their cart; its wording is
// AbandonedCartReminderEmailContent
data class AbandonedCartReminderEmail(val recipient: Email, val customerName: FullName, val cart: Cart)
