package org.flexstore.domain.order.usecase

import org.flexstore.domain.email.*
import org.flexstore.domain.order.*
import org.flexstore.domain.settings.*
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.entity.*

// Sends the confirmation of a placed order to its customer, when the shop settings enable it
class SendOrderConfirmationEmailUseCase(
    private val shopSettingsRepository: ShopSettingsRepository,
    private val userRepository: UserRepository,
    private val emailRepository: EmailRepository
) : UseCase<Order> {

    override fun getPreConditions(): PreConditions<Order> = PreConditions.none()

    override fun getNominalScenario(): NominalScenario<Order> {
        val sendEmailStep = Step<Order> { order ->
            if (shopSettingsRepository.load().sendsOrderConfirmationEmail()) {
                val customer = userRepository.findById(order.customer) as User.DefinedUser
                emailRepository.send(OrderConfirmationEmail(customer.email, customer.fullName, order))
            }
        }
        return NominalScenario(sendEmailStep)
    }

    override fun getPostConditions(): PostConditions<Order> = PostConditions.none()
}
