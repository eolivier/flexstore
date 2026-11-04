package org.flexstore.domain.user.usecase

import org.flexstore.domain.user.RefreshSessionRepository
import org.flexstore.domain.user.UserRepository
import org.flexstore.domain.user.model.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// A user who has just signed in gets a session: its refresh token renews their access
class OpenSessionUseCase(
    private val refreshSessionRepository: RefreshSessionRepository,
    private val userRepository: UserRepository
) : UseCase<RefreshSession> {

    override fun getPreConditions(): PreConditions<RefreshSession> = PreConditions(userExistsCondition())

    override fun getNominalScenario(): NominalScenario<RefreshSession> =
        NominalScenario(Step { session -> refreshSessionRepository.save(session) })

    override fun getPostConditions(): PostConditions<RefreshSession> = PostConditions(sessionIsStoredCondition())

    private fun userExistsCondition() = PreCondition<RefreshSession> { session ->
        if (userRepository.notExists(session.userId)) {
            throw UserNotFoundException(ErrorMessage("User ${session.userId.value} not found."))
        }
    }

    private fun sessionIsStoredCondition() = PostCondition<RefreshSession> { session ->
        if (refreshSessionRepository.isUnknown(session.refreshToken)) {
            throw RefreshSessionFailed(ErrorMessage("The session of ${session.userId.value} was not opened."))
        }
    }
}
