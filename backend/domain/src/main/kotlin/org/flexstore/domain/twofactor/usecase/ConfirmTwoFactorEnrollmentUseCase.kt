package org.flexstore.domain.twofactor.usecase

import org.flexstore.domain.sharedkernel.InstantProvider
import org.flexstore.domain.actor.customerId
import org.flexstore.domain.actor.verifyCanAct
import org.flexstore.domain.twofactor.model.TwoFactorCodeInvalidException
import org.flexstore.domain.twofactor.model.TwoFactorEnrollmentNotPendingException
import org.flexstore.domain.twofactor.model.TwoFactorAuthentication
import org.flexstore.domain.twofactor.model.TwoFactorCode
import org.flexstore.domain.user.repository.UserRepository
import org.flexstore.domain.user.model.User
import org.flexstore.domain.user.model.UserId
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

data class TwoFactorEnrollmentConfirmation(val customer: Actor, val code: TwoFactorCode) {

    fun userId() = customer.customerId()
}

// The customer or administrator confirms their pending enrollment with the first code of their authenticator
// app: only then does two-factor authentication become active
class ConfirmTwoFactorEnrollmentUseCase(private val userRepository: UserRepository) :
    UseCase<TwoFactorEnrollmentConfirmation> {

    override fun getPreConditions(): PreConditions<TwoFactorEnrollmentConfirmation> = PreConditions(
        customerCanActCondition(),
        enrollmentIsPendingCondition(),
        codeMatchesCondition()
    )

    override fun getNominalScenario(): NominalScenario<TwoFactorEnrollmentConfirmation> =
        NominalScenario(Step { confirmation ->
            val user = definedUser(confirmation.userId())
            val pending = user.twoFactorAuthentication as TwoFactorAuthentication.PendingConfirmation
            userRepository.save(user.withTwoFactorAuthenticationEnabled(pending.secret))
        })

    override fun getPostConditions(): PostConditions<TwoFactorEnrollmentConfirmation> = PostConditions.none()

    private fun definedUser(userId: UserId) = userRepository.findById(userId) as User.DefinedUser

    private fun pendingEnrollmentOf(userId: UserId): TwoFactorAuthentication.PendingConfirmation {
        val twoFactorAuthentication = definedUser(userId).twoFactorAuthentication
        if (twoFactorAuthentication is TwoFactorAuthentication.PendingConfirmation) {
            return twoFactorAuthentication
        }
        throw TwoFactorEnrollmentNotPendingException(ErrorMessage("No two-factor enrollment is pending."))
    }

    // The customer, resolved before the use case runs, must be a customer or an admin who can act
    private fun customerCanActCondition() = PreCondition<TwoFactorEnrollmentConfirmation> { confirmation ->
        confirmation.customer.verifyCanAct()
    }

    private fun enrollmentIsPendingCondition() = PreCondition<TwoFactorEnrollmentConfirmation> { confirmation ->
        pendingEnrollmentOf(confirmation.userId())
    }

    private fun codeMatchesCondition() = PreCondition<TwoFactorEnrollmentConfirmation> { confirmation ->
        val pending = pendingEnrollmentOf(confirmation.userId())
        if (pending.secret.doesNotMatch(confirmation.code, InstantProvider.now())) {
            throw TwoFactorCodeInvalidException(ErrorMessage("The two-factor code is wrong."))
        }
    }
}
