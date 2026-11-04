package org.flexstore.domain.user.usecase

import org.flexstore.domain.sharedkernel.ErrorMessage
import org.flexstore.domain.user.RefreshSessionRepository
import org.flexstore.domain.user.model.*
import org.ucop.domain.entity.*

// A user signs out: their session is ended, and its refresh token cannot be used again. Ending a session that is
// unknown or already ended changes nothing, so that signing out twice is no error
class RevokeRefreshSessionUseCase(
    private val refreshSessionRepository: RefreshSessionRepository
) : UseCase<SessionEnding> {

    override fun getPreConditions(): PreConditions<SessionEnding> = PreConditions.none()

    override fun getNominalScenario(): NominalScenario<SessionEnding> =
        NominalScenario(Step { ending ->
            if (refreshSessionRepository.isKnown(ending.presented)) {
                val session = refreshSessionRepository.get(ending.presented)
                if (session.isOpen()) {
                    refreshSessionRepository.save(session.revokedAt(ending.sessionMoment))
                }
            }
        })

    override fun getPostConditions(): PostConditions<SessionEnding> =
        PostConditions(tokenCannotBeUsedCondition())

    private fun tokenCannotBeUsedCondition() = PostCondition<SessionEnding> { ending ->
        val token = ending.presented
        if (refreshSessionRepository.isKnown(token) && refreshSessionRepository.get(token).isOpen()) {
            throw RefreshSessionFailed(ErrorMessage("The session was not ended."))
        }
    }
}
