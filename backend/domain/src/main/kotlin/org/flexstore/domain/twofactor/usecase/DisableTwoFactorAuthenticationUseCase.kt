package org.flexstore.domain.twofactor.usecase

import org.flexstore.domain.twofactor.model.TwoFactorAuthenticationNotEnabledException
import org.flexstore.domain.twofactor.model.TwoFactorAuthentication
import org.flexstore.domain.user.repository.UserRepository
import org.flexstore.domain.user.model.PlainPassword
import org.flexstore.domain.user.model.User
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.usecase.InvalidCurrentPasswordException
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

data class TwoFactorDisabling(val userId: UserId, val currentPassword: PlainPassword)

// The customer or administrator disables their own two-factor authentication, confirmed with their current
// password: a code could be stolen or unavailable (a changed phone), while the password is always what
// protects the account
class DisableTwoFactorAuthenticationUseCase(private val userRepository: UserRepository) :
    UseCase<TwoFactorDisabling> {

    override fun getPreConditions(): PreConditions<TwoFactorDisabling> = PreConditions(
        customerCanActCondition(),
        twoFactorAuthenticationIsEnabledOrPendingCondition(),
        currentPasswordMatchesCondition()
    )

    override fun getNominalScenario(): NominalScenario<TwoFactorDisabling> =
        NominalScenario(Step { disabling ->
            userRepository.save(definedUser(disabling.userId).withTwoFactorAuthenticationDisabled())
        })

    override fun getPostConditions(): PostConditions<TwoFactorDisabling> = PostConditions.none()

    private fun definedUser(userId: UserId) = userRepository.findById(userId) as User.DefinedUser

    // The user must be an existing user whose account is not suspended, locked or disabled (read from the
    // users, not from the request)
    private fun customerCanActCondition() = PreCondition<TwoFactorDisabling> { disabling ->
        userRepository.verifyCustomerCanAct(disabling.userId)
    }

    private fun twoFactorAuthenticationIsEnabledOrPendingCondition() =
        PreCondition<TwoFactorDisabling> { disabling ->
            if (definedUser(disabling.userId).twoFactorAuthentication is TwoFactorAuthentication.Disabled) {
                throw TwoFactorAuthenticationNotEnabledException(
                    ErrorMessage("Two-factor authentication is not enabled.")
                )
            }
        }

    private fun currentPasswordMatchesCondition() = PreCondition<TwoFactorDisabling> { disabling ->
        val email = definedUser(disabling.userId).email
        if (userRepository.passwordDoesNotMatch(email, disabling.currentPassword)) {
            throw InvalidCurrentPasswordException(ErrorMessage("The current password is wrong."))
        }
    }
}
