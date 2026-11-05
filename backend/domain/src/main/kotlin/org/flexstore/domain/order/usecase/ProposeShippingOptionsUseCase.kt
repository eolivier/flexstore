package org.flexstore.domain.order.usecase

import org.flexstore.domain.cart.*
import org.flexstore.domain.order.*
import org.flexstore.domain.order.model.*
import org.flexstore.domain.settings.ShopSettingsRepository
import org.flexstore.domain.user.repository.UserRepository
import org.ucop.domain.entity.*

// The shipping methods a customer chooses from when checking out, with their cost for the items of their cart
class ProposeShippingOptionsUseCase(
    private val cartRepository: CartRepository,
    private val shopSettingsRepository: ShopSettingsRepository,
    private val userRepository: UserRepository
) : QueryUseCase<CartOwner.Customer, ShippingOptions> {

    override fun getPreConditions(): PreConditions<CartOwner.Customer> =
        PreConditions(customerCanActCondition())

    override fun getNominalQuery() = NominalQuery<CartOwner.Customer, ShippingOptions> { customer ->
        val itemsPrice = cartRepository.load(customer).items.totalPrice()
        val freeShippingThreshold = shopSettingsRepository.load().freeShippingThreshold
        ShippingOptions.forItems(itemsPrice, freeShippingThreshold)
    }

    override fun getPostConditions(): PostConditions<ShippingOptions> = PostConditions.none()

    // The customer must be an existing user whose account is not suspended, locked or disabled (read
    // from the users, not from the request)
    private fun customerCanActCondition() = PreCondition<CartOwner.Customer> { customer ->
        userRepository.verifyCustomerCanAct(customer.userId)
    }
}
