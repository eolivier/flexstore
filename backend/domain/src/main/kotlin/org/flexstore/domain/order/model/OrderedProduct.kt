package org.flexstore.domain.order.model

import org.flexstore.domain.cart.*
import org.flexstore.domain.product.*
import org.flexstore.domain.product.model.*
import org.flexstore.domain.sharedkernel.*
import org.flexstore.domain.user.model.*

// A product as ordered: its id and its name when the order was placed
data class OrderedProduct(val productId: ProductId, val productName: Name)
