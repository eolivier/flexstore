package org.flexstore.domain.user.usecase

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.*
import org.ucop.domain.entity.*
import java.time.Instant

data class VerificationEmailRequest(val userId: UserId, val requestedAt: Instant)

// Sends a verification link, valid for 1 hour, to a customer whose email is not verified yet;
// a new link replaces the previous ones
class SendVerificationEmailUseCase(
    private val userRepository: UserRepository,
    private val emailVerificationRepository: EmailVerificationRepository,
    private val accountEmailRepository: AccountEmailRepository
) : UseCase<VerificationEmailRequest> {

    override fun getPreConditions(): PreConditions<VerificationEmailRequest> =
        PreConditions(accountExistsCondition(), accountIsPendingVerificationCondition())

    override fun getNominalScenario(): NominalScenario<VerificationEmailRequest> =
        NominalScenario(Step { request ->
            val account = account(request.userId)
            val verification = EmailVerification.requestedAt(account.id, request.requestedAt)
            emailVerificationRepository.deleteAllOf(account.id)
            emailVerificationRepository.save(verification)
            accountEmailRepository.send(EmailVerificationEmail(account.email, account.fullName, verification.token, verification.expiresAt))
        })

    override fun getPostConditions(): PostConditions<VerificationEmailRequest> = PostConditions.none()

    private fun account(userId: UserId) = userRepository.findById(userId) as User.DefinedUser

    private fun accountExistsCondition() = PreCondition<VerificationEmailRequest> { request ->
        if (userRepository.notExists(request.userId)) {
            throw UserNotFoundException(ErrorMessage("User with ID ${request.userId.value} does not exist."))
        }
    }

    private fun accountIsPendingVerificationCondition() = PreCondition<VerificationEmailRequest> { request ->
        if (account(request.userId).hasVerifiedEmail()) {
            throw AccountAlreadyVerifiedException(ErrorMessage("The email of user ${request.userId.value} is already verified."))
        }
    }
}
