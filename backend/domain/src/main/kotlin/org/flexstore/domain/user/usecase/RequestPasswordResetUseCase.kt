package org.flexstore.domain.user.usecase

import org.flexstore.domain.email.*
import org.flexstore.domain.user.*
import org.flexstore.domain.user.model.*
import org.ucop.domain.entity.*
import java.time.Instant

data class PasswordResetRequest(val email: Email, val requestedAt: Instant)

// A customer who forgot their password asks for a link to choose a new one, valid for 1 hour; a new link
// replaces the previous one. An unknown email gets no link but the same answer: the shop does not tell which
// emails have an account
class RequestPasswordResetUseCase(
    private val userRepository: UserRepository,
    private val passwordResetRepository: PasswordResetRepository,
    private val emailRepository: EmailRepository
) : UseCase<PasswordResetRequest> {

    override fun getPreConditions(): PreConditions<PasswordResetRequest> = PreConditions.none()

    override fun getNominalScenario(): NominalScenario<PasswordResetRequest> =
        NominalScenario(Step { request ->
            val user = userRepository.findByEmail(request.email)
            if (user is User.DefinedUser) sendLink(user, request.requestedAt)
        })

    override fun getPostConditions(): PostConditions<PasswordResetRequest> = PostConditions.none()

    private fun sendLink(user: User.DefinedUser, requestedAt: Instant) {
        val reset = PasswordReset.requestedAt(user.id, requestedAt)
        passwordResetRepository.deleteAllOf(user.id)
        passwordResetRepository.save(reset)
        emailRepository.send(PasswordResetEmail(user, reset))
    }
}
