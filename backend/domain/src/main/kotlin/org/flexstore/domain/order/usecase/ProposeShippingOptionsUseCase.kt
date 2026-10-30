package org.flexstore.domain.order.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.settings.ShopSettingsRepository
import org.ucop.domain.entity.*

// The shipping methods a customer chooses from when checking out, with their cost for the items of their cart
class ProposeShippingOptionsUseCase(
    private val cartRepository: CartRepository,
    private val shopSettingsRepository: ShopSettingsRepository
) : QueryUseCase<CartOwner.Customer, ShippingOptions> {

    override fun getPreConditions(): PreConditions<CartOwner.Customer> = PreConditions.none()

    override fun getNominalQuery() = NominalQuery<CartOwner.Customer, ShippingOptions> { customer ->
        val itemsPrice = cartRepository.load(customer).items.totalPrice()
        val freeShippingThreshold = shopSettingsRepository.load().freeShippingThreshold
        ShippingOptions.forItems(itemsPrice, freeShippingThreshold)
    }

    override fun getPostConditions(): PostConditions<ShippingOptions> = PostConditions.none()
}
