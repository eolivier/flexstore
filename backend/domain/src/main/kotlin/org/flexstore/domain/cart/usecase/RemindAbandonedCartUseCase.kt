package org.flexstore.domain.cart.usecase

import org.flexstore.domain.actor.cannotRunScheduledUseCases
import org.flexstore.domain.cart.*
import org.flexstore.domain.email.EmailRepository
import org.flexstore.domain.email.model.AbandonedCartReminderEmail
import org.flexstore.domain.user.model.User
import org.flexstore.domain.user.repository.UserRepository
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A customer who left items in their cart is reminded of it by email, once: the cart is marked reminded until it
// changes again. A customer whose account cannot act, or whose cart is empty, is not reminded
class RemindAbandonedCartUseCase(
    private val cartRepository: CartRepository,
    private val userRepository: UserRepository,
    private val emailRepository: EmailRepository
) : UseCase<AbandonedCartReminding> {

    override fun getPreConditions(): PreConditions<AbandonedCartReminding> =
        PreConditions(
            actorCanRunScheduledUseCasesCondition(), customerCanActCondition(), cartHoldsItemsCondition()
        )

    // The reminder is sent first: a failure leaves the cart to be reminded at the next run
    override fun getNominalScenario(): NominalScenario<AbandonedCartReminding> {
        val sendReminderStep = Step<AbandonedCartReminding> { reminding ->
            val customer = userRepository.findById(reminding.customer.userId) as User.DefinedUser
            val cart = cartRepository.load(reminding.customer)
            emailRepository.send(AbandonedCartReminderEmail(customer.email, customer.fullName, cart))
        }
        val markRemindedStep = Step<AbandonedCartReminding> { reminding ->
            cartRepository.markReminded(reminding.customer)
        }
        return NominalScenario(listOf(sendReminderStep, markRemindedStep))
    }

    override fun getPostConditions(): PostConditions<AbandonedCartReminding> =
        PostConditions(cartIsRemindedCondition())

    // Only an Admin or the system reminds the customers that a scheduler finds
    private fun actorCanRunScheduledUseCasesCondition() = PreCondition<AbandonedCartReminding> { reminding ->
        if (reminding.actor.cannotRunScheduledUseCases()) {
            throw AbandonedCartForbiddenException(
                ErrorMessage("Only an administrator or the system can remind an abandoned cart.")
            )
        }
    }

    private fun customerCanActCondition() = PreCondition<AbandonedCartReminding> { reminding ->
        userRepository.verifyCustomerCanAct(reminding.customer.userId)
    }

    private fun cartHoldsItemsCondition() = PreCondition<AbandonedCartReminding> { reminding ->
        if (cartRepository.load(reminding.customer).isEmpty()) {
            val message = ErrorMessage("The cart of ${reminding.customer.userId.value} is empty: no reminder.")
            throw AbandonedCartReminderRefusedException(message)
        }
    }

    private fun cartIsRemindedCondition() = PostCondition<AbandonedCartReminding> { reminding ->
        if (cartRepository.isNotReminded(reminding.customer)) {
            val message = ErrorMessage("The cart of ${reminding.customer.userId.value} was not marked reminded.")
            throw AbandonedCartReminderFailed(message)
        }
    }
}
