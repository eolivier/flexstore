package org.flexstore.domain.user.usecase

import org.flexstore.domain.user.repository.RefreshSessionRepository
import org.flexstore.domain.user.repository.UserRepository
import org.flexstore.domain.user.model.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A user whose access token expired renews it with their refresh token: the session is revoked and replaced by
// another one, so that a refresh token works once. The token must be known, not already used, not expired, and
// the account must still allow signing in
class RotateRefreshSessionUseCase(
    private val refreshSessionRepository: RefreshSessionRepository,
    private val userRepository: UserRepository
) : UseCase<SessionRotation> {

    override fun getPreConditions(): PreConditions<SessionRotation> = PreConditions(
        tokenIsKnownCondition(), tokenIsNotReusedCondition(), tokenIsNotExpiredCondition(),
        accountAllowsSignInCondition()
    )

    override fun getNominalScenario(): NominalScenario<SessionRotation> =
        NominalScenario(Step { rotation ->
            val session = refreshSessionRepository.get(rotation.presented)
            refreshSessionRepository.save(session.revokedAt(rotation.sessionMoment))
            refreshSessionRepository.save(rotation.next)
        })

    override fun getPostConditions(): PostConditions<SessionRotation> =
        PostConditions(sessionIsReplacedCondition())

    // Only called once the token is known
    private fun sessionOf(rotation: SessionRotation) = refreshSessionRepository.get(rotation.presented)

    private fun tokenIsKnownCondition() = PreCondition<SessionRotation> { rotation ->
        if (refreshSessionRepository.isUnknown(rotation.presented)) {
            throw RefreshTokenUnknownException(ErrorMessage("This refresh token is unknown."))
        }
    }

    // A token that was already used (or whose session was ended) is a replay
    private fun tokenIsNotReusedCondition() = PreCondition<SessionRotation> { rotation ->
        if (sessionOf(rotation).isRevoked()) {
            throw RefreshTokenReusedException(ErrorMessage("This refresh token was already used."))
        }
    }

    private fun tokenIsNotExpiredCondition() = PreCondition<SessionRotation> { rotation ->
        if (sessionOf(rotation).isExpiredAt(rotation.sessionMoment)) {
            throw RefreshTokenExpiredException(ErrorMessage("This refresh token has expired."))
        }
    }

    // A deleted user has no session left; a suspended, locked or disabled account cannot renew its access
    private fun accountAllowsSignInCondition() = PreCondition<SessionRotation> { rotation ->
        val userId = sessionOf(rotation).userId
        if (userRepository.notExists(userId)) {
            throw RefreshTokenUnknownException(ErrorMessage("This refresh token is unknown."))
        }
        val status = (userRepository.findById(userId) as User.DefinedUser).status
        if (status.refusesSignIn()) {
            throw AccountSignInRefusedException(status, ErrorMessage("The account ${userId.value} is $status."))
        }
    }

    private fun sessionIsReplacedCondition() = PostCondition<SessionRotation> { rotation ->
        val previousIsOpen = sessionOf(rotation).isOpen()
        if (previousIsOpen || refreshSessionRepository.isUnknown(rotation.next.refreshToken)) {
            throw RefreshSessionFailed(ErrorMessage("The session was not renewed."))
        }
    }
}
