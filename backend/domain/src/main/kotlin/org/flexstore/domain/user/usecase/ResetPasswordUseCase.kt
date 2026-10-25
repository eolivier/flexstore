package org.flexstore.domain.user.usecase

import org.flexstore.domain.email.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.entity.*
import java.time.Instant

// The new password a customer chooses on the page of the link they received
data class ChosenPassword(val token: VerificationToken, val password: PlainPassword)

data class PasswordResetConfirmation(val chosenPassword: ChosenPassword, val confirmedAt: Instant)

// The customer opens the link of the reset email and chooses a new password: they sign in with it from now on,
// and the link cannot be used again
class ResetPasswordUseCase(
    private val userRepository: UserRepository,
    private val passwordResetRepository: PasswordResetRepository
) : UseCase<PasswordResetConfirmation> {

    override fun getPreConditions(): PreConditions<PasswordResetConfirmation> =
        PreConditions(linkIsKnownCondition(), linkIsNotExpiredCondition(), passwordIsLongEnoughCondition())

    override fun getNominalScenario(): NominalScenario<PasswordResetConfirmation> =
        NominalScenario(Step { confirmation ->
            val reset = reset(confirmation)
            val user = userRepository.findById(reset.userId) as User.DefinedUser
            userRepository.save(user.copy(password = confirmation.chosenPassword.password))
            passwordResetRepository.deleteAllOf(user.id)
        })

    override fun getPostConditions(): PostConditions<PasswordResetConfirmation> =
        PostConditions(linkIsUsedCondition())

    // Only called once the link is known
    private fun reset(confirmation: PasswordResetConfirmation) =
        passwordResetRepository.findBy(confirmation.chosenPassword.token)!!

    private fun linkIsKnownCondition() = PreCondition<PasswordResetConfirmation> { confirmation ->
        if (passwordResetRepository.isUnknown(confirmation.chosenPassword.token)) {
            val message = ErrorMessage("This link to choose a new password is unknown or was already used.")
            throw PasswordResetLinkUnknownException(message)
        }
    }

    private fun linkIsNotExpiredCondition() = PreCondition<PasswordResetConfirmation> { confirmation ->
        if (reset(confirmation).isExpiredAt(confirmation.confirmedAt)) {
            val message = ErrorMessage("This link to choose a new password has expired.")
            throw PasswordResetLinkExpiredException(message)
        }
    }

    private fun passwordIsLongEnoughCondition() = PreCondition<PasswordResetConfirmation> { confirmation ->
        if (confirmation.chosenPassword.password.value.length < PASSWORD_MIN_LENGTH) {
            val message = ErrorMessage("Password must be at least $PASSWORD_MIN_LENGTH characters.")
            throw InvalidPasswordException(message)
        }
    }

    private fun linkIsUsedCondition() = PostCondition<PasswordResetConfirmation> { confirmation ->
        if (!passwordResetRepository.isUnknown(confirmation.chosenPassword.token)) {
            throw PasswordResetFailed(ErrorMessage("The link to choose a new password can still be used."))
        }
    }
}
