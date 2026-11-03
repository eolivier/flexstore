package org.flexstore.infra.repository

import org.flexstore.domain.user.FailedSignInRepository
import org.flexstore.domain.user.model.FailedSignInCount
import org.flexstore.domain.user.model.UserId

class InMemoryFailedSignInRepository : FailedSignInRepository {

    private val counts = mutableMapOf<UserId, FailedSignInCount>()

    override fun countOf(userId: UserId): FailedSignInCount = counts[userId] ?: FailedSignInCount.none()

    override fun save(userId: UserId, failedSignInCount: FailedSignInCount) {
        counts[userId] = failedSignInCount
    }

    override fun reset(userId: UserId) {
        counts.remove(userId)
    }
}
