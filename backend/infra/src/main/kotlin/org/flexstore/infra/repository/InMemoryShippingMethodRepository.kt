package org.flexstore.infra.repository

import org.flexstore.domain.order.ShippingMethodRepository
import org.flexstore.domain.order.model.*

// The shipping methods in memory, the cheapest first, starting with the default ones: for the tests
class InMemoryShippingMethodRepository : ShippingMethodRepository {

    private val methods = ShippingMethods.byDefault().asList().toMutableList()

    override fun save(method: ShippingMethod) {
        val index = methods.indexOfFirst { it.id == method.id }
        if (index < 0) methods.add(method) else methods[index] = method
    }

    override fun findBy(shippingMethodId: ShippingMethodId): ShippingMethod? =
        methods.firstOrNull { it.id == shippingMethodId }

    override fun findAll() = ShippingMethods(methods.sortedBy { it.cost.amount.value })
}
