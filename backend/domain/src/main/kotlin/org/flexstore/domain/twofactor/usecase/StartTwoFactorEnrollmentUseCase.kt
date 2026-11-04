package org.flexstore.domain.twofactor.usecase

import org.flexstore.domain.twofactor.TwoFactorAuthenticationAlreadyEnabledException
import org.flexstore.domain.twofactor.model.TwoFactorAuthentication
import org.flexstore.domain.twofactor.model.TwoFactorSecret
import org.flexstore.domain.user.UserRepository
import org.flexstore.domain.user.model.User
import org.flexstore.domain.user.model.UserId
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// The id of the secret is chosen by the caller (TwoFactorAuthenticationService), which already generated it
data class TwoFactorEnrollmentStart(val userId: UserId, val secret: TwoFactorSecret)

// A signed-in customer or administrator starts the enrollment of their own two-factor authentication: the
// secret is kept, but not activated yet (see ConfirmTwoFactorEnrollmentUseCase)
class StartTwoFactorEnrollmentUseCase(private val userRepository: UserRepository) :
    UseCase<TwoFactorEnrollmentStart> {

    override fun getPreConditions(): PreConditions<TwoFactorEnrollmentStart> = PreConditions(
        customerCanActCondition(),
        twoFactorAuthenticationIsNotEnabledCondition()
    )

    override fun getNominalScenario(): NominalScenario<TwoFactorEnrollmentStart> =
        NominalScenario(Step { start ->
            userRepository.save(definedUser(start.userId).withTwoFactorAuthenticationPending(start.secret))
        })

    override fun getPostConditions(): PostConditions<TwoFactorEnrollmentStart> = PostConditions.none()

    private fun definedUser(userId: UserId) = userRepository.findById(userId) as User.DefinedUser

    // The user must be an existing user whose account is not suspended, locked or disabled (read from the
    // users, not from the request)
    private fun customerCanActCondition() = PreCondition<TwoFactorEnrollmentStart> { start ->
        userRepository.verifyCustomerCanAct(start.userId)
    }

    // A new enrollment may replace one still pending, but not one already enabled: it is disabled first
    private fun twoFactorAuthenticationIsNotEnabledCondition() =
        PreCondition<TwoFactorEnrollmentStart> { start ->
            if (definedUser(start.userId).twoFactorAuthentication is TwoFactorAuthentication.Enabled) {
                throw TwoFactorAuthenticationAlreadyEnabledException(
                    ErrorMessage("Two-factor authentication is already enabled.")
                )
            }
        }
}
