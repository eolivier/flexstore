package org.flexstore.domain.user.usecase

import org.flexstore.domain.user.repository.RefreshSessionRepository
import org.flexstore.domain.user.model.*
import org.ucop.domain.ErrorMessage
import org.ucop.domain.entity.*

// The session of a refresh token: whose it is, and whether it was already used
class ReadRefreshSessionUseCase(
    private val refreshSessionRepository: RefreshSessionRepository
) : QueryUseCase<RefreshToken, RefreshSession> {

    override fun getPreConditions(): PreConditions<RefreshToken> = PreConditions(tokenIsKnownCondition())

    override fun getNominalQuery() = NominalQuery<RefreshToken, RefreshSession> { token ->
        refreshSessionRepository.get(token)
    }

    override fun getPostConditions(): PostConditions<RefreshSession> = PostConditions.none()

    private fun tokenIsKnownCondition() = PreCondition<RefreshToken> { token ->
        if (refreshSessionRepository.isUnknown(token)) {
            throw RefreshTokenUnknownException(ErrorMessage("This refresh token is unknown."))
        }
    }
}
