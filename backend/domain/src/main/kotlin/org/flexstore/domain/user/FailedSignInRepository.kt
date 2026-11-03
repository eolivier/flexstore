package org.flexstore.domain.user

import org.flexstore.domain.user.model.FailedSignInCount
import org.flexstore.domain.user.model.UserId

interface FailedSignInRepository {

    // The failed sign-ins in a row of an account: none when it has none
    fun countOf(userId: UserId): FailedSignInCount

    fun save(userId: UserId, failedSignInCount: FailedSignInCount)

    // A successful sign-in starts the count again
    fun reset(userId: UserId)
}
