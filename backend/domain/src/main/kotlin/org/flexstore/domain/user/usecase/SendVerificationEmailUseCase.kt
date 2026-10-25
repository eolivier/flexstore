package org.flexstore.domain.user.usecase

import org.flexstore.domain.email.*
import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.entity.*
import java.time.Instant

data class VerificationEmailRequest(val userId: UserId, val requestedAt: Instant)

// Sends a verification link, valid for 1 hour, to a customer whose email is not verified yet;
// a new link expires the previous ones: opening one of them tells the customer it has expired
class SendVerificationEmailUseCase(
    private val userRepository: UserRepository,
    private val emailRepository: EmailRepository,
    private val verificationPage: VerificationPage
) : UseCase<VerificationEmailRequest> {

    override fun getPreConditions(): PreConditions<VerificationEmailRequest> =
        PreConditions(accountExistsCondition(), accountNeedsVerificationCondition())

    override fun getNominalScenario(): NominalScenario<VerificationEmailRequest> =
        NominalScenario(Step { request ->
            val user = user(request.userId)
            val verification = EmailVerification.requestedAt(user.id, request.requestedAt)
            emailRepository.expireAllVerificationsOf(user.id, ExpirationDate(request.requestedAt))
            emailRepository.save(verification)
            emailRepository.send(EmailVerificationLink(user.email, user.fullName, verificationPage, verification.token, verification.expiresAt))
        })

    override fun getPostConditions(): PostConditions<VerificationEmailRequest> = PostConditions.none()

    private fun user(userId: UserId) = userRepository.findById(userId) as User.DefinedUser

    private fun accountExistsCondition() = PreCondition<VerificationEmailRequest> { request ->
        if (userRepository.notExists(request.userId)) {
            throw UserNotFoundException(ErrorMessage("User with ID ${request.userId.value} does not exist."))
        }
    }

    private fun accountNeedsVerificationCondition() = PreCondition<VerificationEmailRequest> { request ->
        if (EmailVerification.isAlreadyDoneFor(user(request.userId))) {
            throw AccountAlreadyVerifiedException(ErrorMessage("The email of user ${request.userId.value} is already verified."))
        }
    }
}
