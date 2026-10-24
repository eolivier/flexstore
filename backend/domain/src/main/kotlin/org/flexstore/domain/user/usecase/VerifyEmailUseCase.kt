package org.flexstore.domain.user.usecase

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.*
import org.ucop.domain.entity.*
import java.time.Instant

data class EmailVerificationConfirmation(val token: VerificationToken, val confirmedAt: Instant)

// The customer opens the link of the verification email: their account becomes active, and the link cannot be used again
class VerifyEmailUseCase(
    private val userRepository: UserRepository,
    private val emailVerificationRepository: EmailVerificationRepository
) : UseCase<EmailVerificationConfirmation> {

    override fun getPreConditions(): PreConditions<EmailVerificationConfirmation> =
        PreConditions(linkIsKnownCondition(), linkIsNotExpiredCondition())

    override fun getNominalScenario(): NominalScenario<EmailVerificationConfirmation> =
        NominalScenario(Step { confirmation ->
            val verification = verification(confirmation)!!
            val account = userRepository.findById(verification.userId) as User.DefinedUser
            userRepository.save(account.withEmailVerified())
            emailVerificationRepository.deleteAllOf(account.id)
        })

    override fun getPostConditions(): PostConditions<EmailVerificationConfirmation> = PostConditions.none()

    private fun verification(confirmation: EmailVerificationConfirmation) = emailVerificationRepository.findBy(confirmation.token)

    private fun linkIsKnownCondition() = PreCondition<EmailVerificationConfirmation> { confirmation ->
        if (emailVerificationRepository.isUnknown(confirmation.token)) {
            throw VerificationLinkUnknownException(ErrorMessage("This verification link is unknown or was already used."))
        }
    }

    private fun linkIsNotExpiredCondition() = PreCondition<EmailVerificationConfirmation> { confirmation ->
        if (verification(confirmation)!!.isExpiredAt(confirmation.confirmedAt)) {
            throw VerificationLinkExpiredException(ErrorMessage("This verification link has expired."))
        }
    }
}
