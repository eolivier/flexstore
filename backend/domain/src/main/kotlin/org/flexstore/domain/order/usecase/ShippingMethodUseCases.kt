package org.flexstore.domain.order.usecase

import org.flexstore.domain.cart.CartOwner
import org.flexstore.domain.order.model.*
import org.ucop.domain.entity.Actor

// The use cases of the shipping methods: the administrators manage them, the customers choose one to check out
class ShippingMethodUseCases(
    private val listShippingMethodsUseCase: ListShippingMethodsUseCase,
    private val createShippingMethodUseCase: CreateShippingMethodUseCase,
    private val updateShippingMethodUseCase: UpdateShippingMethodUseCase,
    private val readEnabledShippingMethodUseCase: ReadEnabledShippingMethodUseCase
) {
    fun getAll(reader: Actor) = listShippingMethodsUseCase.unfold(ShippingMethodsReading(reader))

    fun create(creator: Actor, method: ShippingMethod) =
        createShippingMethodUseCase.unfold(ShippingMethodCreation(creator, method))

    fun update(editor: Actor, method: ShippingMethod) =
        updateShippingMethodUseCase.unfold(ShippingMethodUpdate(editor, method))

    fun getEnabled(customer: CartOwner.Customer, shippingMethodId: ShippingMethodId) =
        readEnabledShippingMethodUseCase.unfold(EnabledShippingMethodLookup(customer, shippingMethodId))
}
