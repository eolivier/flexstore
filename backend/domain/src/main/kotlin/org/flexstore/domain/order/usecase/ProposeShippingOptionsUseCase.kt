package org.flexstore.domain.order.usecase

import org.flexstore.domain.actor.customerId
import org.flexstore.domain.actor.verifyCanAct
import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.*
import org.flexstore.domain.settings.ShopSettingsRepository
import org.ucop.domain.entity.*

// A customer reads the shipping methods they choose from
data class ShippingOptionsReading(val customer: Actor)

// The shipping methods a customer chooses from when checking out, with their cost for the items of their cart
class ProposeShippingOptionsUseCase(
    private val cartRepository: CartRepository,
    private val shippingMethodRepository: ShippingMethodRepository,
    private val shopSettingsRepository: ShopSettingsRepository
) : QueryUseCase<ShippingOptionsReading, ShippingOptions> {

    override fun getPreConditions(): PreConditions<ShippingOptionsReading> =
        PreConditions(customerCanActCondition())

    override fun getNominalQuery() = NominalQuery<ShippingOptionsReading, ShippingOptions> { reading ->
        val itemsPrice = cartRepository.load(CartOwner.Customer(reading.customer.customerId())).items.totalPrice()
        val freeShippingThreshold = shopSettingsRepository.load().freeShippingThreshold
        ShippingOptions.forItems(shippingMethodRepository.findAll(), itemsPrice, freeShippingThreshold)
    }

    override fun getPostConditions(): PostConditions<ShippingOptions> = PostConditions.none()

    // The customer, resolved before the use case runs, must be a customer or an admin who can act
    private fun customerCanActCondition() = PreCondition<ShippingOptionsReading> { reading ->
        reading.customer.verifyCanAct()
    }
}
