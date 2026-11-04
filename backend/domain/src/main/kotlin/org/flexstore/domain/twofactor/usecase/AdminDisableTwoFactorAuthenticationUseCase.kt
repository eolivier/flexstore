package org.flexstore.domain.twofactor.usecase

import org.flexstore.domain.twofactor.InvalidTwoFactorAuthenticationChangeException
import org.flexstore.domain.twofactor.TwoFactorAuthenticationForbiddenException
import org.flexstore.domain.twofactor.TwoFactorAuthenticationNotEnabledException
import org.flexstore.domain.twofactor.model.TwoFactorAuthentication
import org.flexstore.domain.user.UserRepository
import org.flexstore.domain.user.model.User
import org.flexstore.domain.user.model.UserId
import org.flexstore.domain.user.model.UserNotFoundException
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// An administrator (the editor) disables the two-factor authentication of another user, from the user
// administration screen: no current password is asked, unlike when an administrator removes it from their own
// account (DisableTwoFactorAuthenticationUseCase)
data class AdminTwoFactorDisabling(val editor: UserId, val userId: UserId)

class AdminDisableTwoFactorAuthenticationUseCase(private val userRepository: UserRepository) :
    UseCase<AdminTwoFactorDisabling> {

    override fun getPreConditions(): PreConditions<AdminTwoFactorDisabling> = PreConditions(
        editorIsAdministratorCondition(),
        accountExistsCondition(),
        accountIsNotTheEditorsCondition(),
        twoFactorAuthenticationIsEnabledOrPendingCondition()
    )

    override fun getNominalScenario(): NominalScenario<AdminTwoFactorDisabling> =
        NominalScenario(Step { disabling ->
            userRepository.save(user(disabling.userId).withTwoFactorAuthenticationDisabled())
        })

    override fun getPostConditions(): PostConditions<AdminTwoFactorDisabling> = PostConditions.none()

    private fun user(userId: UserId) = userRepository.findById(userId) as User.DefinedUser

    // The editor must be an existing user with the ADMIN role (read from the users, not from the request)
    private fun editorIsAdministratorCondition() = PreCondition<AdminTwoFactorDisabling> { disabling ->
        if (userRepository.isNotAdministrator(disabling.editor)) {
            val message = ErrorMessage("Only an administrator can disable the two-factor authentication of a user.")
            throw TwoFactorAuthenticationForbiddenException(message)
        }
    }

    private fun accountExistsCondition() = PreCondition<AdminTwoFactorDisabling> { disabling ->
        if (userRepository.notExists(disabling.userId)) {
            throw UserNotFoundException(ErrorMessage("User with ID ${disabling.userId.value} does not exist."))
        }
    }

    // An administrator who disables their own two-factor authentication confirms it with their current password,
    // through the dedicated self-service operation, instead of this administration one
    private fun accountIsNotTheEditorsCondition() = PreCondition<AdminTwoFactorDisabling> { disabling ->
        if (disabling.userId == disabling.editor) {
            val message = ErrorMessage(
                "An administrator cannot disable the two-factor authentication of their own account this way."
            )
            throw InvalidTwoFactorAuthenticationChangeException(message)
        }
    }

    private fun twoFactorAuthenticationIsEnabledOrPendingCondition() =
        PreCondition<AdminTwoFactorDisabling> { disabling ->
            if (user(disabling.userId).twoFactorAuthentication is TwoFactorAuthentication.Disabled) {
                throw TwoFactorAuthenticationNotEnabledException(
                    ErrorMessage("Two-factor authentication is not enabled.")
                )
            }
        }
}
