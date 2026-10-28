package org.flexstore.domain.order.usecase

import org.flexstore.domain.email.*
import org.flexstore.domain.order.*
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.entity.*

// Tells the customer that their order reached a status announced to them (shipped, delivered); the other
// statuses send nothing, nor an order whose customer cannot be read any more
class SendOrderStatusEmailUseCase(
    private val userRepository: UserRepository,
    private val emailRepository: EmailRepository
) : UseCase<Order> {

    override fun getPreConditions(): PreConditions<Order> = PreConditions.none()

    override fun getNominalScenario(): NominalScenario<Order> {
        val sendEmailStep = Step<Order> { order ->
            val customer = customerOf(order)
            if (order.status.isAnnouncedToTheCustomer() && customer is User.DefinedUser) {
                emailRepository.send(OrderStatusEmail(customer.email, customer.fullName, order))
            }
        }
        return NominalScenario(sendEmailStep)
    }

    override fun getPostConditions(): PostConditions<Order> = PostConditions.none()

    private fun customerOf(order: Order): User {
        if (userRepository.exists(order.customer)) return userRepository.findById(order.customer)
        return User.UndefinedUser(order.customer, "The customer of the order cannot be read")
    }
}
