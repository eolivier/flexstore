package org.flexstore.domain.twofactor.usecase

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.sharedkernel.InstantProvider
import org.flexstore.domain.twofactor.TwoFactorAuthenticationForbiddenException
import org.flexstore.domain.twofactor.TwoFactorCodeInvalidException
import org.flexstore.domain.twofactor.TwoFactorEnrollmentNotPendingException
import org.flexstore.domain.twofactor.model.TwoFactorAuthentication
import org.flexstore.domain.twofactor.model.TwoFactorCode
import org.flexstore.domain.user.UserRepository
import org.flexstore.domain.user.model.User
import org.flexstore.domain.user.model.UserId
import org.ucop.domain.entity.*

data class TwoFactorEnrollmentConfirmation(val admin: UserId, val code: TwoFactorCode)

// The administrator confirms their pending enrollment with the first code of their authenticator app: only then
// does two-factor authentication become active
class ConfirmTwoFactorEnrollmentUseCase(private val userRepository: UserRepository) :
    UseCase<TwoFactorEnrollmentConfirmation> {

    override fun getPreConditions(): PreConditions<TwoFactorEnrollmentConfirmation> = PreConditions(
        adminIsAdministratorCondition(),
        enrollmentIsPendingCondition(),
        codeMatchesCondition()
    )

    override fun getNominalScenario(): NominalScenario<TwoFactorEnrollmentConfirmation> =
        NominalScenario(Step { confirmation ->
            val user = definedUser(confirmation.admin)
            val pending = user.twoFactorAuthentication as TwoFactorAuthentication.PendingConfirmation
            userRepository.save(user.withTwoFactorAuthenticationEnabled(pending.secret))
        })

    override fun getPostConditions(): PostConditions<TwoFactorEnrollmentConfirmation> = PostConditions.none()

    private fun definedUser(userId: UserId) = userRepository.findById(userId) as User.DefinedUser

    private fun pendingEnrollmentOf(admin: UserId): TwoFactorAuthentication.PendingConfirmation {
        val twoFactorAuthentication = definedUser(admin).twoFactorAuthentication
        if (twoFactorAuthentication !is TwoFactorAuthentication.PendingConfirmation) {
            throw TwoFactorEnrollmentNotPendingException(ErrorMessage("No two-factor enrollment is pending."))
        }
        return twoFactorAuthentication
    }

    private fun adminIsAdministratorCondition() = PreCondition<TwoFactorEnrollmentConfirmation> { confirmation ->
        if (userRepository.isNotAdministrator(confirmation.admin)) {
            throw TwoFactorAuthenticationForbiddenException(
                ErrorMessage("Only an administrator can enable two-factor authentication.")
            )
        }
    }

    private fun enrollmentIsPendingCondition() = PreCondition<TwoFactorEnrollmentConfirmation> { confirmation ->
        pendingEnrollmentOf(confirmation.admin)
    }

    private fun codeMatchesCondition() = PreCondition<TwoFactorEnrollmentConfirmation> { confirmation ->
        val pending = pendingEnrollmentOf(confirmation.admin)
        if (pending.secret.doesNotMatch(confirmation.code, InstantProvider.now())) {
            throw TwoFactorCodeInvalidException(ErrorMessage("The two-factor code is wrong."))
        }
    }
}
