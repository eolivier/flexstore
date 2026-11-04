package org.flexstore.domain.twofactor.usecase

import org.flexstore.domain.twofactor.TwoFactorAuthenticationAlreadyEnabledException
import org.flexstore.domain.twofactor.TwoFactorAuthenticationForbiddenException
import org.flexstore.domain.twofactor.model.TwoFactorAuthentication
import org.flexstore.domain.twofactor.model.TwoFactorSecret
import org.flexstore.domain.user.UserRepository
import org.flexstore.domain.user.model.User
import org.flexstore.domain.user.model.UserId
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// The id of the secret is chosen by the caller (TwoFactorAuthenticationService), which already generated it
data class TwoFactorEnrollmentStart(val admin: UserId, val secret: TwoFactorSecret)

// An administrator starts the enrollment of their own two-factor authentication: the secret is kept, but not
// activated yet (see ConfirmTwoFactorEnrollmentUseCase). Restricted to ADMIN accounts in this iteration
class StartTwoFactorEnrollmentUseCase(private val userRepository: UserRepository) :
    UseCase<TwoFactorEnrollmentStart> {

    override fun getPreConditions(): PreConditions<TwoFactorEnrollmentStart> = PreConditions(
        adminIsAdministratorCondition(),
        twoFactorAuthenticationIsNotEnabledCondition()
    )

    override fun getNominalScenario(): NominalScenario<TwoFactorEnrollmentStart> =
        NominalScenario(Step { start ->
            userRepository.save(definedUser(start.admin).withTwoFactorAuthenticationPending(start.secret))
        })

    override fun getPostConditions(): PostConditions<TwoFactorEnrollmentStart> = PostConditions.none()

    private fun definedUser(userId: UserId) = userRepository.findById(userId) as User.DefinedUser

    private fun adminIsAdministratorCondition() = PreCondition<TwoFactorEnrollmentStart> { start ->
        if (userRepository.isNotAdministrator(start.admin)) {
            throw TwoFactorAuthenticationForbiddenException(
                ErrorMessage("Only an administrator can enable two-factor authentication.")
            )
        }
    }

    // A new enrollment may replace one still pending, but not one already enabled: it is disabled first
    private fun twoFactorAuthenticationIsNotEnabledCondition() =
        PreCondition<TwoFactorEnrollmentStart> { start ->
            if (definedUser(start.admin).twoFactorAuthentication is TwoFactorAuthentication.Enabled) {
                throw TwoFactorAuthenticationAlreadyEnabledException(
                    ErrorMessage("Two-factor authentication is already enabled.")
                )
            }
        }
}
