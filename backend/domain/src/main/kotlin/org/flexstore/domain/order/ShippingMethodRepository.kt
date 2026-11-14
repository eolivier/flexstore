package org.flexstore.domain.order

import org.flexstore.domain.order.model.ShippingMethod
import org.flexstore.domain.order.model.ShippingMethodId
import org.flexstore.domain.order.model.ShippingMethodNotFoundException
import org.flexstore.domain.order.model.ShippingMethods

// The shipping methods managed by the administrators
interface ShippingMethodRepository {

    // Adds the method, or replaces the one with the same id
    fun save(method: ShippingMethod)

    fun findBy(shippingMethodId: ShippingMethodId): ShippingMethod?

    // The shipping method, which a precondition has checked to exist
    fun get(shippingMethodId: ShippingMethodId): ShippingMethod =
        findBy(shippingMethodId) ?: throw ShippingMethodNotFoundException("Shipping method $shippingMethodId not found")

    // The cheapest first
    fun findAll(): ShippingMethods

    fun shippingMethodExists(shippingMethodId: ShippingMethodId) = findBy(shippingMethodId) != null

    fun shippingMethodIsMissing(shippingMethodId: ShippingMethodId) = !shippingMethodExists(shippingMethodId)
}
