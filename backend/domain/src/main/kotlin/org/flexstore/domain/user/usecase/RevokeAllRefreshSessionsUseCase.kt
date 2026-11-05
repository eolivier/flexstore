package org.flexstore.domain.user.usecase

import org.flexstore.domain.user.repository.RefreshSessionRepository
import org.flexstore.domain.user.model.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// Every session of a user is ended: a refresh token that was already used came back, so it was stolen or
// replayed
class RevokeAllRefreshSessionsUseCase(
    private val refreshSessionRepository: RefreshSessionRepository
) : UseCase<AllSessionsEnding> {

    override fun getPreConditions(): PreConditions<AllSessionsEnding> = PreConditions.none()

    override fun getNominalScenario(): NominalScenario<AllSessionsEnding> =
        NominalScenario(Step { ending ->
            refreshSessionRepository.revokeAllOf(ending.userId, ending.sessionMoment)
        })

    override fun getPostConditions(): PostConditions<AllSessionsEnding> =
        PostConditions(noSessionIsOpenCondition())

    private fun noSessionIsOpenCondition() = PostCondition<AllSessionsEnding> { ending ->
        if (refreshSessionRepository.hasOpenSessionOf(ending.userId)) {
            val message = ErrorMessage("The sessions of ${ending.userId.value} were not all ended.")
            throw RefreshSessionFailed(message)
        }
    }
}
